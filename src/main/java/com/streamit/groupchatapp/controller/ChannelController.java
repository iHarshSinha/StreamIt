package com.streamit.groupchatapp.controller;


import com.streamit.groupchatapp.dto.ChannelRequestDTO;
import com.streamit.groupchatapp.dto.ChannelResponseDTO;
import com.streamit.groupchatapp.dto.ChannelResponseDetailedDTO;
import com.streamit.groupchatapp.model.Message;
import com.streamit.groupchatapp.model.User;
import com.streamit.groupchatapp.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChannelController {
    private final ChannelService channelService;

    @GetMapping("/channels")
    public List<ChannelResponseDTO> getChannels() {
        return channelService.getChannels();
    }

    @GetMapping("/channels/{id}")
    public ChannelResponseDetailedDTO getChannel(@PathVariable Long id) {
        return channelService.getChannel(id);
    }

    @GetMapping("/channels/{channelId}/open")
    public ChannelResponseDetailedDTO openChannel(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "30") int limit,
            @RequestParam(required = false) Long cursor,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        return channelService.openChannel(channelId, user.getId(), limit, cursor);
    }


    @PostMapping("/channels")
    public ChannelResponseDTO createChannel(@RequestBody ChannelRequestDTO channelRequest) {
        return channelService.createChannel(channelRequest);
    }

    @PostMapping("/channels/{channelId}/join")
    public ChannelResponseDTO joinChannel(@PathVariable Long channelId) {
        return channelService.addUser(channelId);
    }

    @PostMapping("/channels/{channelId}/leave")
    public ChannelResponseDTO leaveChannel(@PathVariable Long channelId) {
        return channelService.removeUser(channelId);
    }

    @PostMapping("/channels/{channelId}/test-send")
    public ResponseEntity<?> testSendMessage(
            @PathVariable Long channelId,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String content = body.get("content");

        User sender = (User) authentication.getPrincipal();

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
