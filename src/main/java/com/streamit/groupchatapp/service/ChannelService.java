package com.streamit.groupchatapp.service;

import com.streamit.groupchatapp.dto.*;
import com.streamit.groupchatapp.exception.ResourceNotFoundException;
import com.streamit.groupchatapp.mapper.ChannelMapper;
import com.streamit.groupchatapp.model.Channel;
import com.streamit.groupchatapp.model.ChannelMembership;
import com.streamit.groupchatapp.model.Message;
import com.streamit.groupchatapp.model.User;
import com.streamit.groupchatapp.model.enums.ChannelRole;
import com.streamit.groupchatapp.model.enums.Status;
import com.streamit.groupchatapp.repository.ChannelRepository;
import com.streamit.groupchatapp.repository.MembershipRepository;
import com.streamit.groupchatapp.repository.MessageRepository;
import com.streamit.groupchatapp.repository.UserRepository;
import com.streamit.groupchatapp.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final MessageRepository messageRepository;

    @Transactional(readOnly = true)
    public List<ChannelResponseDTO> getChannels(UserPrincipal userPrincipal) {

        List<Channel> channels = channelRepository.findAllForUserOrPublic(userPrincipal.id());

        return channels.stream()
                .map(ChannelMapper::toResponse)
                .toList();
    }





    public ChannelResponseDTO createChannel(ChannelRequestDTO channelRequest, UserPrincipal userPrincipal) {

        Channel channel = Channel.builder()
                .channelName(channelRequest.getChannelName())
                .channelDescription(channelRequest.getChannelDescription())
                .type(channelRequest.getType())
                .build();

        User user = userRepository.getReferenceById(userPrincipal.id());

        channelRepository.save(channel);
        ChannelMembership member = ChannelMembership.builder()
                .channel(channel)
                .user(user)
                .role(ChannelRole.ADMIN)
                .status(Status.ACTIVE)
                .build();

        membershipRepository.save(member);

        return ChannelMapper.toResponse(channel);

    }

    @Transactional
    public ChannelResponseDTO addUser(Long channelId, UserPrincipal userPrincipal) {


        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + channelId));

        boolean alreadyJoined = membershipRepository.existsByChannelIdAndUserId(channelId, userPrincipal.id());

        if (alreadyJoined) {
            // return channel response anyway (idempotent)
            return ChannelMapper.toResponse(channel);
        }
        User user = userRepository.getReferenceById(userPrincipal.id());

        ChannelMembership member = ChannelMembership.builder()
                .channel(channel)
                .user(user)
                .role(ChannelRole.MEMBER)
                .status(Status.ACTIVE)
                .build();

        membershipRepository.save(member);

        return ChannelMapper.toResponse(channel);
    }


    @Transactional
    public ChannelResponseDTO removeUser(Long channelId, UserPrincipal  userPrincipal) {


        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found with id: " + channelId));

        membershipRepository.deleteByChannelIdAndUserId(channelId, userPrincipal.id());

        return ChannelMapper.toResponse(channel);
    }



    @Transactional(readOnly = true)
    public ChannelResponseDetailedDTO openChannel(Long channelId, Long userId, int limit, Long cursor) {

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found"));

        ChannelMembership membership = membershipRepository
                .findByChannelIdAndUserId(channelId, userId)
                .orElse(null);

        boolean isMember = membership != null;

        // If private channel → block access unless member
        if (channel.getType().equals("PRIVATE") && !isMember) {
            throw new AccessDeniedException("Not a member of this channel");
        }

        List<Message> messages = messageRepository.fetchMessages(channelId, cursor, Pageable.ofSize(limit));

        List<MessageDTO> messageDTOs = messages.stream().map(m -> MessageDTO.builder()
                .id(m.getId())
                .content(m.getText())
                .sentAt(m.getCreatedAt())
                .sender(MessageSenderDTO.builder()
                        .id(m.getSender().getId())
                        .name(m.getSender().getName())
                        .profileImageUrl(m.getSender().getProfileImageUrl())
                        .build())
                .build()
        ).toList();

        Long nextCursor = messages.isEmpty() ? null : messages.getLast().getId();

        return ChannelResponseDetailedDTO.builder()
                .id(channel.getId())
                .channelName(channel.getChannelName())
                .channelDescription(channel.getChannelDescription())
                .createdOn(channel.getCreatedOn())
                .type(channel.getType())
                .viewer(ViewerMembershipDTO.builder()
                        .isMember(isMember)
                        .role(isMember ? membership.getRole().name() : null)
                        .status(isMember ? membership.getStatus().name() : null)
                        .joinedAt(isMember ? membership.getJoinedAt() : null)
                        .build())
                .messages(messageDTOs)
                .nextCursor(nextCursor)
                .build();
    }


    @Transactional
    public Message testSendMessage(Long channelId, UserPrincipal sender, String content) {

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("content cannot be null/empty");
        }

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel not found: " + channelId));

        // Optional but strongly recommended
        boolean isMember = membershipRepository.existsByChannelIdAndUserId(channelId, sender.id());
        if (!isMember) {
            throw new AccessDeniedException("User is not a member of this channel");
        }
         User user =  userRepository.getReferenceById(sender.id());

        Message msg = Message.builder()
                .channel(channel)
                .sender(user)                 // adjust to your entity field name
                .text(content)
                .createdAt(LocalDateTime.now())    // adjust to your entity field name
                .build();

        return messageRepository.save(msg);
    }
}
