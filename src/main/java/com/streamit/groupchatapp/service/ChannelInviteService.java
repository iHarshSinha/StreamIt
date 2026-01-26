package com.streamit.groupchatapp.service;

import com.streamit.groupchatapp.dto.ChannelInviteDTO;
import com.streamit.groupchatapp.exception.ResourceNotFoundException;
import com.streamit.groupchatapp.model.*;
import com.streamit.groupchatapp.model.enums.ChannelRole;
import com.streamit.groupchatapp.model.enums.InviteStatus;
import com.streamit.groupchatapp.model.enums.Status;
import com.streamit.groupchatapp.repository.*;
import com.streamit.groupchatapp.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
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

    private static final int INVITE_EXPIRY_DAYS = 2;

    @Transactional
    public ChannelInviteDTO sendInvite(Long channelId, Long invitedUserId, UserPrincipal userPrincipal) {


        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found"));

        ChannelMembership membership = membershipRepository
                .findByChannelIdAndUserId(channelId, userPrincipal.id())
                .orElseThrow(() -> new AccessDeniedException("You are not a member"));

        if (!membership.getRole().equals(ChannelRole.ADMIN)) {
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
                .expiresAt(now.plusDays(INVITE_EXPIRY_DAYS))
                .build();

        inviteRepository.save(invite);

        return mapToDTO(invite);
    }

    @Transactional(readOnly = true)
    public List<ChannelInviteDTO> getMyInvites(UserPrincipal userPrincipal, boolean pendingOnly) {
        List<ChannelInvite> invites = pendingOnly
                ? inviteRepository.findByInvitedUserIdAndStatusOrderByCreatedAtDesc(userPrincipal.id(), InviteStatus.PENDING)
                : inviteRepository.findByInvitedUserIdOrderByCreatedAtDesc(userPrincipal.id());

        return invites.stream().map(this::mapToDTO).toList();
    }

    @Transactional
    public void acceptInvite(Long inviteId, UserPrincipal userPrincipal) {
        ChannelInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found"));

        validateInviteOwnership(invite, userPrincipal);
        validateNotExpired(invite);

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new IllegalArgumentException("Invite is not pending");
        }

        User user  = userRepository.getReferenceById(userPrincipal.id());

        // Create membership
        ChannelMembership membership = ChannelMembership.builder()
                .channel(invite.getChannel())
                .user(user)
                .role(ChannelRole.MEMBER)       // or your enum
                .status(Status.ACTIVE)     // or your enum
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

        validateInviteOwnership(invite, userPrincipal);
        validateNotExpired(invite);

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new IllegalArgumentException("Invite is not pending");
        }

        invite.setStatus(InviteStatus.REJECTED);
        inviteRepository.save(invite);
    }

    private void
    validateInviteOwnership(ChannelInvite invite, UserPrincipal currentUser) {
        if (!invite.getInvitedUser().getId().equals(currentUser.id())) {
            throw new AccessDeniedException("Not authorized to modify this invite");
        }
    }

    private void validateNotExpired(ChannelInvite invite) {
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            invite.setStatus(InviteStatus.EXPIRED);
            inviteRepository.save(invite);
            throw new IllegalArgumentException("Invite expired");
        }
    }

    private ChannelInviteDTO mapToDTO(ChannelInvite invite) {
        return ChannelInviteDTO.builder()
                .inviteId(invite.getId())
                .channelId(invite.getChannel().getId())
                .channelName(invite.getChannel().getChannelName())
                .invitedById(invite.getInvitedBy().getId())
                .invitedByName(invite.getInvitedBy().getName())
                .status(invite.getStatus().name())
                .createdAt(invite.getCreatedAt())
                .expiresAt(invite.getExpiresAt())
                .build();
    }
}
