package com.streamit.groupchatapp.controller;

import com.streamit.groupchatapp.dto.ChannelInviteDTO;
import com.streamit.groupchatapp.dto.SendInviteRequestDTO;
import com.streamit.groupchatapp.security.principal.UserPrincipal;
import com.streamit.groupchatapp.service.ChannelInviteService;
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
    // ✅ Admin sends invite to a specific user for a channel
    @PostMapping("/invites/send")
    public ResponseEntity<ChannelInviteDTO> sendInvite(
            @Valid @RequestBody SendInviteRequestDTO request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {

        ChannelInviteDTO response = inviteService.sendInvite(
                request.getChannelId(),
                request.getInvitedUserId(),
                userPrincipal
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ✅ User fetches all their invites
    @GetMapping("/invites/my")
    public ResponseEntity<List<ChannelInviteDTO>> getMyInvites(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "true") boolean pendingOnly
    ) {

        List<ChannelInviteDTO> invites =
                inviteService.getMyInvites(userPrincipal, pendingOnly);

        if (invites.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }

        return ResponseEntity.ok(invites); // 200
    }

    // ✅ User accepts invite
    @PostMapping("/invites/{inviteId}/accept")
    public ResponseEntity<?> acceptInvite(
            @PathVariable Long inviteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        inviteService.acceptInvite(inviteId, userPrincipal);
        return ResponseEntity.ok().build();
    }

    // ✅ User rejects invite
    @PostMapping("/invites/{inviteId}/reject")
    public ResponseEntity<?> rejectInvite(
            @PathVariable Long inviteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        inviteService.rejectInvite(inviteId, userPrincipal);
        return ResponseEntity.ok().build();
    }
}
