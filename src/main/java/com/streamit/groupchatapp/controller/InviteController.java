package com.streamit.groupchatapp.controller;

import com.streamit.groupchatapp.dto.ChannelInviteDTO;
import com.streamit.groupchatapp.dto.SendInviteRequestDTO;
import com.streamit.groupchatapp.model.User;
import com.streamit.groupchatapp.security.principal.UserPrincipal;
import com.streamit.groupchatapp.service.ChannelInviteService;
import com.streamit.groupchatapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class InviteController {

    private final ChannelInviteService inviteService;
    private final UserService userService;
    // ✅ Admin sends invite to a specific user for a channel
    @PostMapping("/invites/send")
    public ResponseEntity<ChannelInviteDTO> sendInvite(
            @Valid @RequestBody SendInviteRequestDTO request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        User currentUser =
                userService.findByEmail(userPrincipal.email());

        ChannelInviteDTO response = inviteService.sendInvite(
                request.getChannelId(),
                request.getInvitedUserId(),
                currentUser
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ✅ User fetches all their invites
    @GetMapping("/invites/my")
    public ResponseEntity<List<ChannelInviteDTO>> getMyInvites(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "true") boolean pendingOnly
    ) {
        User user = userService.findByEmail(userPrincipal.email());

        List<ChannelInviteDTO> invites =
                inviteService.getMyInvites(user, pendingOnly);

        if (invites.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }

        return ResponseEntity.ok(invites); // 200
    }

    // ✅ User accepts invite
    @PostMapping("/invites/{inviteId}/accept")
    public void acceptInvite(
            @PathVariable Long inviteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        User user = userService.findByEmail(userPrincipal.email());
        inviteService.acceptInvite(inviteId, user);
    }

    // ✅ User rejects invite
    @PostMapping("/invites/{inviteId}/reject")
    public void rejectInvite(
            @PathVariable Long inviteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        User user = userService.findByEmail(userPrincipal.email());
        inviteService.rejectInvite(inviteId, user);
    }
}
