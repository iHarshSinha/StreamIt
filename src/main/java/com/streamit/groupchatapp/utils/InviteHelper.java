package com.streamit.groupchatapp.utils;

import com.streamit.groupchatapp.model.ChannelInvite;
import com.streamit.groupchatapp.model.enums.invite.InviteStatus;
import com.streamit.groupchatapp.repository.ChannelInviteRepository;
import com.streamit.groupchatapp.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public final class InviteHelper {
    private final ChannelInviteRepository inviteRepository;
    public void validateInviteOwnership(ChannelInvite invite, UserPrincipal currentUser) {
        if (!invite.getInvitedUser().getId().equals(currentUser.id())) {
            throw new AccessDeniedException("Not authorized to modify this invite");
        }
    }

    public void validateNotExpired(ChannelInvite invite) {
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            invite.setStatus(InviteStatus.EXPIRED);
            inviteRepository.save(invite);
            throw new IllegalArgumentException("Invite expired");
        }
    }
}
