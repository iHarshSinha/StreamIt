package com.streamit.groupchatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String email;
    private String name;
    private String profileImageUrl;
}
