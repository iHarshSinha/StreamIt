package com.streamit.groupchatapp.security.jwt;

import com.streamit.groupchatapp.model.User;
import com.streamit.groupchatapp.repository.MembershipRepository;
import com.streamit.groupchatapp.repository.UserRepository;
import com.streamit.groupchatapp.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Missing Authorization header");
            }

            String token = authHeader.substring(7);
            if (!jwtService.isTokenValid(token)) {
                throw new IllegalArgumentException("Invalid JWT");
            }

            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            UserPrincipal principal = UserPrincipal.create(user);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.getAuthorities()
                    );

            accessor.setUser(auth);
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Principal principal = accessor.getUser();
            if (principal == null) {
                throw new IllegalArgumentException("No authenticated user");
            }

            String destination = accessor.getDestination();
            Long channelId = extractChannelId(destination); // implement helper

            UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();
            boolean allowed = membershipRepository
                    .existsByChannelIdAndUserId(channelId, userPrincipal.id());

            if (!allowed) {
                throw new IllegalArgumentException("Unauthorized subscription");
            }
        }

        return message;
    }

    private Long extractChannelId(String destination) {
        // expected: /topic/channel/{id}
        String[] parts = destination.split("/");
        return Long.parseLong(parts[parts.length - 1]);
    }
}