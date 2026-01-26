package com.streamit.groupchatapp.controller;


import com.streamit.groupchatapp.dto.ChannelRequestDTO;
import com.streamit.groupchatapp.dto.ChannelResponseDTO;
import com.streamit.groupchatapp.dto.ChannelResponseDetailedDTO;
import com.streamit.groupchatapp.model.Message;
import com.streamit.groupchatapp.security.principal.UserPrincipal;
import com.streamit.groupchatapp.service.ChannelService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class ChannelController {
    private final ChannelService channelService;

    @GetMapping("/channels")
    public ResponseEntity<List<ChannelResponseDTO>> getChannels(@AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<ChannelResponseDTO> channels = channelService.getChannels(userPrincipal);

        if (channels.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }

        return ResponseEntity.ok(channels); // 200
    }



    @GetMapping("/channels/{channelId}/open")
    public ResponseEntity<ChannelResponseDetailedDTO> openChannel(
            @PathVariable @Min(value = 1, message = "Channel id must be greater than 0") Long channelId,
            @RequestParam(defaultValue = "30") int limit,
            @RequestParam(required = false) Long cursor,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        // ✅ success
        return ResponseEntity.ok(channelService.openChannel(
                channelId,
                userPrincipal.id(),
                limit,
                cursor
        )); // 200
    }


    @PostMapping("/channels")
    public ResponseEntity<ChannelResponseDTO> createChannel(
            @Valid @RequestBody ChannelRequestDTO channelRequest, @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        ChannelResponseDTO response = channelService.createChannel(channelRequest, userPrincipal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/channels/{channelId}/join")
    public ResponseEntity<ChannelResponseDTO> joinChannel(
            @PathVariable
            @Min(value = 1, message = "Channel id must be greater than 0")
            Long channelId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        ChannelResponseDTO response = channelService.addUser(channelId, userPrincipal);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/channels/{channelId}/leave")
    public ResponseEntity<ChannelResponseDTO> leaveChannel(@PathVariable @Min(value = 1, message = "Channel id must be greater than 0") Long channelId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(channelService.removeUser(channelId, userPrincipal));
    }

    @PostMapping("/channels/{channelId}/test-send")
    public ResponseEntity<?> testSendMessage(
            @PathVariable Long channelId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        String content = body.get("content");



        Message saved = channelService.testSendMessage(channelId, userPrincipal, content);

        Map<String, Object> response = Map.of(
                "id", saved.getId(),
                "channelId", saved.getChannel().getId(),
                "senderId", userPrincipal.id(),
                "content", saved.getText(),
                "sentAt", saved.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }

}
