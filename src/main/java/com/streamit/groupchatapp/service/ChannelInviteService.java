package com.streamit.groupchatapp.service;

import com.streamit.groupchatapp.dto.ChannelInviteDTO;
import com.streamit.groupchatapp.model.*;
import com.streamit.groupchatapp.model.enums.ChannelRole;
import com.streamit.groupchatapp.model.enums.InviteStatus;
import com.streamit.groupchatapp.model.enums.Status;
import com.streamit.groupchatapp.repository.*;
import lombok.RequiredArgsConstructor;
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
    public ChannelInviteDTO sendInvite(Long channelId, Long invitedUserId, User currentUser) {

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        ChannelMembership membership = membershipRepository
                .findByChannelIdAndUserId(channelId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("You are not a member"));

        if (!membership.getRole().equals(ChannelRole.ADMIN)) {
            throw new RuntimeException("Only admins can invite users");
        }

        User invitedUser = userRepository.findById(invitedUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // You can enforce "only admins/owners can invite" here
        // Example: check currentUser membership role == ADMIN/OWNER

        // Prevent inviting someone already a member
        boolean alreadyMember = membershipRepository.existsByChannelIdAndUserId(channelId, invitedUserId);
        if (alreadyMember) {
            throw new RuntimeException("User is already a member of this channel");
        }

        // Prevent duplicate pending invite
        inviteRepository.findByChannelIdAndInvitedUserIdAndStatus(channelId, invitedUserId, InviteStatus.PENDING)
                .ifPresent(inv -> { throw new RuntimeException("Invite already pending"); });

        LocalDateTime now = LocalDateTime.now();

        ChannelInvite invite = ChannelInvite.builder()
                .channel(channel)
                .invitedUser(invitedUser)
                .invitedBy(currentUser)
                .status(InviteStatus.PENDING)
                .createdAt(now)
                .expiresAt(now.plusDays(INVITE_EXPIRY_DAYS))
                .build();

        inviteRepository.save(invite);

        return mapToDTO(invite);
    }

    @Transactional(readOnly = true)
    public List<ChannelInviteDTO> getMyInvites(User currentUser, boolean pendingOnly) {
        List<ChannelInvite> invites = pendingOnly
                ? inviteRepository.findByInvitedUserIdAndStatusOrderByCreatedAtDesc(currentUser.getId(), InviteStatus.PENDING)
                : inviteRepository.findByInvitedUserIdOrderByCreatedAtDesc(currentUser.getId());

        return invites.stream().map(this::mapToDTO).toList();
    }

    @Transactional
    public void acceptInvite(Long inviteId, User currentUser) {
        ChannelInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));

        validateInviteOwnership(invite, currentUser);
        validateNotExpired(invite);

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new RuntimeException("Invite is not pending");
        }

        // Create membership
        ChannelMembership membership = ChannelMembership.builder()
                .channel(invite.getChannel())
                .user(currentUser)
                .role(ChannelRole.MEMBER)       // or your enum
                .status(Status.ACTIVE)     // or your enum
                .joinedAt(LocalDateTime.now())
                .build();

        membershipRepository.save(membership);

        invite.setStatus(InviteStatus.ACCEPTED);
        inviteRepository.save(invite);
    }

    @Transactional
    public void rejectInvite(Long inviteId, User currentUser) {
        ChannelInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));

        validateInviteOwnership(invite, currentUser);
        validateNotExpired(invite);

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new RuntimeException("Invite is not pending");
        }

        invite.setStatus(InviteStatus.REJECTED);
        inviteRepository.save(invite);
    }

    private void validateInviteOwnership(ChannelInvite invite, User currentUser) {
        if (!invite.getInvitedUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Not authorized to modify this invite");
        }
    }

    private void validateNotExpired(ChannelInvite invite) {
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            invite.setStatus(InviteStatus.EXPIRED);
            inviteRepository.save(invite);
            throw new RuntimeException("Invite expired");
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
