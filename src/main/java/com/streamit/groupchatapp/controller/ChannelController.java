package com.streamit.groupchatapp.controller;


import com.streamit.groupchatapp.dto.ChannelRequestDTO;
import com.streamit.groupchatapp.dto.ChannelResponseDTO;
import com.streamit.groupchatapp.dto.ChannelResponseDetailedDTO;
import com.streamit.groupchatapp.model.Message;
import com.streamit.groupchatapp.model.User;
import com.streamit.groupchatapp.security.principal.UserPrincipal;
import com.streamit.groupchatapp.service.ChannelService;
import com.streamit.groupchatapp.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    private final UserService userService;

    @GetMapping("/channels")
    public ResponseEntity<List<ChannelResponseDTO>> getChannels() {

        List<ChannelResponseDTO> channels = channelService.getChannels();

        if (channels.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }

        return ResponseEntity.ok(channels); // 200
    }

    @GetMapping("/channels/{id}")
    public ResponseEntity<ChannelResponseDetailedDTO> getChannel(
            @PathVariable
            @Min(value = 1, message = "Channel id must be greater than 0")
            Long id
    ) {
        ChannelResponseDetailedDTO channel = channelService.getChannel(id);

        if (channel == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(channel);
    }

    @GetMapping("/channels/{channelId}/open")
    public ResponseEntity<ChannelResponseDetailedDTO> openChannel(
            @PathVariable @Min(value = 1, message = "Channel id must be greater than 0") Long channelId,
            @RequestParam(defaultValue = "30") int limit,
            @RequestParam(required = false) Long cursor,
            Authentication authentication
    ) {
        UserPrincipal userPrincipal =
                (UserPrincipal) authentication.getPrincipal();

        ChannelResponseDetailedDTO response =
                channelService.openChannel(
                        channelId,
                        userPrincipal.id(),
                        limit,
                        cursor
                );

        // ✅ service returned null → resource not found
        if (response == null) {
            return ResponseEntity.notFound().build(); // 404
        }

        // ✅ success
        return ResponseEntity.ok(response); // 200
    }


    @PostMapping("/channels")
    public ResponseEntity<ChannelResponseDTO> createChannel(
            @Valid @RequestBody ChannelRequestDTO channelRequest
    ) {
        ChannelResponseDTO response = channelService.createChannel(channelRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/channels/{channelId}/join")
    public ResponseEntity<ChannelResponseDTO> joinChannel(
            @PathVariable
            @Min(value = 1, message = "Channel id must be greater than 0")
            Long channelId
    ) {
        ChannelResponseDTO response = channelService.addUser(channelId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/channels/{channelId}/leave")
    public ChannelResponseDTO leaveChannel(@PathVariable @Min(value = 1, message = "Channel id must be greater than 0") Long channelId) {
        return channelService.removeUser(channelId);
    }

    @PostMapping("/channels/{channelId}/test-send")
    public ResponseEntity<?> testSendMessage(
            @PathVariable Long channelId,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String content = body.get("content");

        UserPrincipal userPrincipal= (UserPrincipal) authentication.getPrincipal();
        User sender=userService.findByEmail(userPrincipal.email());


        Message saved = channelService.testSendMessage(channelId, sender, content);

        Map<String, Object> response = Map.of(
                "id", saved.getId(),
                "channelId", saved.getChannel().getId(),
                "senderId", sender.getId(),
                "content", saved.getText(),
                "sentAt", saved.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }

}
