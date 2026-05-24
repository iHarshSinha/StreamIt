package com.streamit.groupchatapp.service;

import com.streamit.groupchatapp.dto.ChannelInviteDTO;
import com.streamit.groupchatapp.exception.ResourceNotFoundException;
import com.streamit.groupchatapp.mapper.ChannelInviteMapper;
import com.streamit.groupchatapp.model.*;
import com.streamit.groupchatapp.model.enums.memberChannelRelation.MemberPosition;
import com.streamit.groupchatapp.model.enums.invite.InviteStatus;
import com.streamit.groupchatapp.model.enums.memberChannelRelation.MemberStatus;
import com.streamit.groupchatapp.repository.*;
import com.streamit.groupchatapp.security.principal.UserPrincipal;
import com.streamit.groupchatapp.utils.InviteHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelInviteService {

    private final ChannelInviteRepository inviteRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final ChannelInviteMapper channelInviteMapper;
    private final InviteHelper inviteHelper;

    @Value("${app.invite.expire.duration}")
    private int inviteExpiryDays;

    @Transactional
    public ChannelInviteDTO sendInvite(Long channelId, Long invitedUserId, UserPrincipal userPrincipal) {


        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found"));

        ChannelMembership membership = membershipRepository
                .findByChannelIdAndUserId(channelId, userPrincipal.id())
                .orElseThrow(() -> new AccessDeniedException("You are not a member"));

        if (!membership.getRole().equals(MemberPosition.ADMIN)) {
            throw new AccessDeniedException("Only admins can invite users");
        }

        User invitedUser = userRepository.findById(invitedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));


        // Prevent inviting someone already a member
        boolean alreadyMember = membershipRepository.existsByChannelIdAndUserId(channelId, invitedUserId);
        if (alreadyMember) {
            throw new IllegalArgumentException("User is already a member of this channel");
        }

        // Prevent duplicate pending invite
        inviteRepository.findByChannelIdAndInvitedUserIdAndStatus(channelId, invitedUserId, InviteStatus.PENDING)
                .ifPresent(inv -> { throw new IllegalArgumentException("Invite already pending"); });

        LocalDateTime now = LocalDateTime.now();
        User user = userRepository.getReferenceById(userPrincipal.id());
        ChannelInvite invite = ChannelInvite.builder()
                .channel(channel)
                .invitedUser(invitedUser)
                .invitedBy(user)
                .status(InviteStatus.PENDING)
                .createdAt(now)
                .expiresAt(now.plusDays(inviteExpiryDays))
                .build();

        inviteRepository.save(invite);

        return channelInviteMapper.toDTO(invite);
    }

    @Transactional(readOnly = true)
    public List<ChannelInviteDTO> getMyInvites(UserPrincipal userPrincipal, boolean pendingOnly) {
        List<ChannelInvite> invites = pendingOnly
                ? inviteRepository.findByInvitedUserIdAndStatusOrderByCreatedAtDesc(userPrincipal.id(), InviteStatus.PENDING)
                : inviteRepository.findByInvitedUserIdOrderByCreatedAtDesc(userPrincipal.id());

        return invites.stream()
                .map(channelInviteMapper::toDTO)
                .toList();
    }

    @Transactional
    public void acceptInvite(Long inviteId, UserPrincipal userPrincipal) {
        ChannelInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found"));

        inviteHelper.validateInviteOwnership(invite, userPrincipal);
        inviteHelper.validateNotExpired(invite);

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new IllegalArgumentException("Invite is not pending");
        }

        User user  = userRepository.getReferenceById(userPrincipal.id());

        // Create membership
        ChannelMembership membership = ChannelMembership.builder()
                .channel(invite.getChannel())
                .user(user)
                .role(MemberPosition.MEMBER)       // or your enum
                .personGroupStatus(MemberStatus.ACTIVE)     // or your enum
                .joinedAt(LocalDateTime.now())
                .build();

        membershipRepository.save(membership);

        invite.setStatus(InviteStatus.ACCEPTED);
        inviteRepository.save(invite);
    }

    @Transactional
    public void rejectInvite(Long inviteId, UserPrincipal userPrincipal) {
        ChannelInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found"));

        inviteHelper.validateInviteOwnership(invite, userPrincipal);
        inviteHelper.validateNotExpired(invite);

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new IllegalArgumentException("Invite is not pending");
        }

        invite.setStatus(InviteStatus.REJECTED);
        inviteRepository.save(invite);
    }



}
