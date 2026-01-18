package com.streamit.groupchatapp;

import com.streamit.groupchatapp.security.jwt.JwtService;
import com.streamit.groupchatapp.model.User;
import com.streamit.groupchatapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtTestRunner {

    @Bean
    CommandLineRunner generateTestToken(
            JwtService jwtService,
            UserRepository userRepository
    ) {
        return args -> {
            User user = userRepository.findByEmail("test@streamit.com")
                    .orElseThrow();

            String token = jwtService.generateToken(
                    user.getEmail(),
                    user.getRole(),
                    user.getId(),
                    user.getName(),
                    user.getProfileImageUrl()
            );

            System.out.println("==================================");
            System.out.println("TEST JWT TOKEN:");
            System.out.println(token);
            System.out.println("==================================");
        };
    }
}