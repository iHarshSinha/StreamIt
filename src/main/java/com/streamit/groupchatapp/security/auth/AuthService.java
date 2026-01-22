package com.streamit.groupchatapp.security.auth;

import com.streamit.groupchatapp.security.jwt.JwtService;
import com.streamit.groupchatapp.model.User;
import com.streamit.groupchatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final StringRedisTemplate redisTemplate;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public String exchangeAuthCode(String code) {
        String redisKey = "authcode:" + code;

        String accessToken = redisTemplate.opsForValue().get(redisKey);

        if (accessToken == null) {
            throw new IllegalArgumentException("Invalid or expired authorization code");
        }

        // 🔥 One-time use
        redisTemplate.delete(redisKey);

        return accessToken;
    }

    public String refreshAccessToken(String refreshToken) {
        System.out.println("method called\n\n\n\n");

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token missing");
        }

        // 🔍 Validate refresh token in Redis
        String userIdStr = redisTemplate.opsForValue()
                .get("refresh:" + refreshToken);

        if (userIdStr == null) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        Long userId = Long.valueOf(userIdStr);

        // 🔄 Load user (safe check)
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        // 🔐 Generate new access token
        return jwtService.generateToken(
                user.getEmail(),
                user.getId(),
                user.getName(),
                user.getProfileImageUrl()
        );
    }
    public void logout(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return; // idempotent logout
        }

        // 🗑 Remove refresh token from Redis
        redisTemplate.delete("refresh:" + refreshToken);
    }
}