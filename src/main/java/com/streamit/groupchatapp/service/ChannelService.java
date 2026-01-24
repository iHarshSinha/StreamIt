    package com.streamit.groupchatapp.service;

    import com.streamit.groupchatapp.dto.*;
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
    import lombok.RequiredArgsConstructor;
    import org.springframework.data.domain.Pageable;
    import org.springframework.http.HttpStatus;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import org.springframework.web.server.ResponseStatusException;

    import java.time.LocalDateTime;
    import java.util.ArrayList;
    import java.util.List;


    @Service
    @RequiredArgsConstructor
    public class ChannelService {

        private final ChannelRepository channelRepository;
        private final UserRepository userRepository;
        private final MembershipRepository membershipRepository;
        private final MessageRepository messageRepository;

        @Transactional(readOnly = true)
        public List<ChannelResponseDTO> getChannels() {
            User user = getCurrentUser();

            List<Channel> channels = channelRepository.findAllForUserOrPublic(user.getId());

            return channels.stream()
                    .map(ChannelMapper::toResponse)
                    .toList();
        }



        @Transactional(readOnly = true)
        public ChannelResponseDetailedDTO getChannel(Long id) {
            Channel channel = channelRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Channel not found with id: " + id));

            return ChannelMapper.toDetailedResponse(channel);
        }


        public ChannelResponseDTO createChannel(ChannelRequestDTO channelRequest) {

            Channel channel = Channel.builder()
                    .channelName(channelRequest.getChannelName())
                    .channelDescription(channelRequest.getChannelDescription())
                    .type(channelRequest.getType())
                    .build();

            channelRepository.save(channel);

            User user = getCurrentUser();

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
        public ChannelResponseDTO addUser(Long channelId) {

            User user = getCurrentUser();

            Channel channel = channelRepository.findById(channelId)
                    .orElseThrow(() -> new RuntimeException("Channel not found with id: " + channelId));

            boolean alreadyJoined = membershipRepository.existsByChannelIdAndUserId(channelId, user.getId());

            if (alreadyJoined) {
                // return channel response anyway (idempotent)
                return ChannelMapper.toResponse(channel);
            }

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
        public ChannelResponseDTO removeUser(Long channelId) {

            User user = getCurrentUser();

            Channel channel = channelRepository.findById(channelId)
                    .orElseThrow(() -> new RuntimeException("Channel not found with id: " + channelId));

            membershipRepository.deleteByChannelIdAndUserId(channelId, user.getId());

            return ChannelMapper.toResponse(channel);
        }



        private User getCurrentUser() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !auth.isAuthenticated()) {
                throw new RuntimeException("Unauthenticated request");
            }

            Object principal = auth.getPrincipal();

            if (principal instanceof User user) {
                return user;
            }

            throw new RuntimeException("Unexpected principal type: " + principal.getClass());
        }


        @Transactional(readOnly = true)
        public ChannelResponseDetailedDTO openChannel(Long channelId, Long userId, int limit, Long cursor) {

            Channel channel = channelRepository.findById(channelId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Channel not found"));

            ChannelMembership membership = membershipRepository
                    .findByChannelIdAndUserId(channelId, userId)
                    .orElse(null);

            boolean isMember = membership != null;

            // If private channel → block access unless member
            if (channel.getType().equals("PRIVATE") && !isMember) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this channel");
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

            Long nextCursor = messages.isEmpty() ? null : messages.get(messages.size() - 1).getId();

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
        public Message testSendMessage(Long channelId, User sender, String content) {

            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("content cannot be null/empty");
            }

            Channel channel = channelRepository.findById(channelId)
                    .orElseThrow(() -> new RuntimeException("Channel not found: " + channelId));

            // Optional but strongly recommended
            boolean isMember = membershipRepository.existsByChannelIdAndUserId(channelId, sender.getId());
            if (!isMember) {
                throw new RuntimeException("User is not a member of this channel");
            }

            Message msg = Message.builder()
                    .channel(channel)
                    .sender(sender)                 // adjust to your entity field name
                    .text(content)
                    .createdAt(LocalDateTime.now())    // adjust to your entity field name
                    .build();

            return messageRepository.save(msg);
        }
    }
