package com.streamit.groupchatapp.service;

import com.streamit.groupchatapp.dto.MessageDTO;
import com.streamit.groupchatapp.dto.MessageSenderDTO;
import com.streamit.groupchatapp.dto.WsMessageRequest;
import com.streamit.groupchatapp.exception.ResourceNotFoundException;
import com.streamit.groupchatapp.model.Channel;
import com.streamit.groupchatapp.model.Message;
import com.streamit.groupchatapp.model.User;
import com.streamit.groupchatapp.repository.ChannelRepository;
import com.streamit.groupchatapp.repository.MembershipRepository;
import com.streamit.groupchatapp.repository.MessageRepository;
import com.streamit.groupchatapp.repository.UserRepository;
import com.streamit.groupchatapp.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void sendMessage(WsMessageRequest request, UserPrincipal sender) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("content cannot be empty");
        }

        Long channelId = request.getChannelId();

        boolean isMember = membershipRepository
                .existsByChannelIdAndUserId(channelId, sender.id());
        if (!isMember) {
            throw new AccessDeniedException("Not a member of this channel");
        }

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found"));

        User user = userRepository.getReferenceById(sender.id());

        Message msg = Message.builder()
                .channel(channel)
                .sender(user)
                .text(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        Message saved = messageRepository.save(msg);

        MessageDTO payload = MessageDTO.builder()
                .id(saved.getId())
                .content(saved.getText())
                .sentAt(saved.getCreatedAt())
                .sender(MessageSenderDTO.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .profileImageUrl(user.getProfileImageUrl())
                        .build())
                .build();

        messagingTemplate.convertAndSend(
                "/topic/channel/" + channelId,
                payload
        );
    }
}
