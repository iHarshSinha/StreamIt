package com.streamit.groupchatapp.mapper;

import com.streamit.groupchatapp.dto.UserResponseDTO;
import com.streamit.groupchatapp.security.principal.UserPrincipal;

public class UserMapper {
    public static UserResponseDTO toResponse(UserPrincipal principal) {
        return UserResponseDTO.builder()
                .id(principal.id())
                .email(principal.email())
                .name(principal.name())
                .profileImageUrl(principal.profileImageUrl())
                .build();
    }
}