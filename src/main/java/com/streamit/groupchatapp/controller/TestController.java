package com.streamit.groupchatapp.controller;

import com.streamit.groupchatapp.dto.UserResponseDTO;
import com.streamit.groupchatapp.mapper.UserMapper;
import com.streamit.groupchatapp.model.User;
import com.streamit.groupchatapp.repository.UserRepository;
import com.streamit.groupchatapp.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> me(Authentication authentication) {
        // as while creating principal object we have already looked in db, we do not need to look it again
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        if(principal==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        return ResponseEntity.ok(UserMapper.toResponse(principal));
    }
}
