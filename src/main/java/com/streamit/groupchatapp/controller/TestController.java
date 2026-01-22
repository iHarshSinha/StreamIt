package com.streamit.groupchatapp.controller;

import com.streamit.groupchatapp.dto.UserResponseDTO;
import com.streamit.groupchatapp.model.User;
import com.streamit.groupchatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;

    @GetMapping("/public")
    public String publicEndpoint() {
        return "Public endpoint works";
    }

    @GetMapping("/private")
    public String privateEndpoint() {
        return "Private endpoint works";
    }

    // ✅ GET /me
    @GetMapping("/me")
    public UserResponseDTO me(Authentication authentication) {

        // This will be the email if your JWT filter sets authentication correctly
        User oauthUser = (User) authentication.getPrincipal();
        String email = oauthUser.getEmail();


        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getProfileImageUrl()
        );
    }
}
