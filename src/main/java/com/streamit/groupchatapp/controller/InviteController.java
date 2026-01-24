package com.streamit.groupchatapp.controller;

import com.streamit.groupchatapp.dto.ChannelInviteDTO;
import com.streamit.groupchatapp.dto.SendInviteRequestDTO;
import com.streamit.groupchatapp.model.User;
import com.streamit.groupchatapp.service.ChannelInviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class InviteController {

    private final ChannelInviteService inviteService;

    // ✅ Admin sends invite to a specific user for a channel
    @PostMapping("/invites/send")
    public ChannelInviteDTO sendInvite(
            @RequestBody SendInviteRequestDTO request,
            @AuthenticationPrincipal User currentUser
    ) {
        return inviteService.sendInvite(
                request.getChannelId(),
                request.getInvitedUserId(),
                currentUser
        );
    }

    // ✅ User fetches all their invites
    @GetMapping("/invites/my")
    public List<ChannelInviteDTO> getMyInvites(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "true") boolean pendingOnly
    ) {
        return inviteService.getMyInvites(user, pendingOnly);
    }

    // ✅ User accepts invite
    @PostMapping("/invites/{inviteId}/accept")
    public void acceptInvite(
            @PathVariable Long inviteId,
            @AuthenticationPrincipal User user
    ) {
        inviteService.acceptInvite(inviteId, user);
    }

    // ✅ User rejects invite
    @PostMapping("/invites/{inviteId}/reject")
    public void rejectInvite(
            @PathVariable Long inviteId,
            @AuthenticationPrincipal User user
    ) {
        inviteService.rejectInvite(inviteId, user);
    }
}
