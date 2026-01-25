package com.streamit.groupchatapp.service;


import com.streamit.groupchatapp.model.User;
import com.streamit.groupchatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    public User findByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found with email: " + email)
                );
    }

}
