package com.streamit.groupchatapp.security.oauth;

import com.streamit.groupchatapp.config.FrontendProperties;
import com.streamit.groupchatapp.model.enums.Status;
import com.streamit.groupchatapp.security.jwt.JwtService;
import com.streamit.groupchatapp.model.User;
import com.streamit.groupchatapp.repository.UserRepository;
import com.streamit.groupchatapp.security.principal.UserPrincipal;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;
    private final FrontendProperties frontendProperties;

    // 🌍 From ENV / properties


    @Value("${auth.refresh-token.days}")
    private int refreshTokenDays;

    @Value("${auth.auth-code.minutes}")
    private int authCodeMinutes;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String profileImageUrl = oauthUser.getAttribute("picture");

        if (email == null) {
            throw new IllegalStateException("Email not found from Google OAuth");
        }

        // 🔍 FIND OR CREATE USER
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .name(name != null ? name : "Unknown")
                                .profileImageUrl(profileImageUrl)
                                .status(Status.ACTIVE)
                                .build()
                ));

        // 🔐 ACCESS TOKEN (JWT)
        String accessToken = jwtService.generateToken(
                UserPrincipal.create(user)
        );

        // 🔑 REFRESH TOKEN (opaque)
        String refreshToken = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(
                "refresh:" + refreshToken,
                user.getId().toString(),
                refreshTokenDays,
                TimeUnit.DAYS
        );

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // true in prod (HTTPS)
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(refreshTokenDays * 24 * 60 * 60);
        response.addCookie(refreshCookie);

        // 🔄 ONE-TIME AUTH CODE
        String authCode = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(
                "authcode:" + authCode,
                accessToken,
                authCodeMinutes,
                TimeUnit.MINUTES
        );

        // 🧹 CLEAN UP OAUTH REQUEST COOKIE
        Cookie cleanup = new Cookie(
                HttpCookieOAuth2AuthorizationRequestRepository
                        .OAUTH2_AUTH_REQUEST_COOKIE_NAME,
                ""
        );
        cleanup.setPath("/");
        cleanup.setMaxAge(0);
        response.addCookie(cleanup);

        // ↩️ REDIRECT TO FRONTEND
        System.out.println("reaching redirect success");
        System.out.println(frontendProperties.getSuccessRedirectUrl()+"?code="+authCode);
        response.sendRedirect(
                frontendProperties.getSuccessRedirectUrl() + "?code=" + authCode
        );
    }
}