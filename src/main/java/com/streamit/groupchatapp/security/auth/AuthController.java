package com.streamit.groupchatapp.security.auth;

import com.streamit.groupchatapp.security.auth.dto.ExchangeTokenRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/exchange-token")
    public ResponseEntity<?> exchangeToken(
            @RequestBody ExchangeTokenRequest request
    ) {
        String token = authService.exchangeAuthCode(request.code());
        return ResponseEntity.ok(Map.of("token", token));
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {

        String refreshToken = extractRefreshToken(request);

        String newAccessToken = authService.refreshAccessToken(refreshToken);

        return ResponseEntity.ok(
                Map.of("token", newAccessToken)
        );
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String refreshToken = extractRefreshToken(request);

        authService.logout(refreshToken);

        // 🍪 Clear cookie in browser
        Cookie clearCookie = new Cookie("refreshToken", "");
        clearCookie.setHttpOnly(true);
        clearCookie.setSecure(false); // true in prod
        clearCookie.setPath("/");
        clearCookie.setMaxAge(0);
        response.addCookie(clearCookie);

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully")
        );
    }
}