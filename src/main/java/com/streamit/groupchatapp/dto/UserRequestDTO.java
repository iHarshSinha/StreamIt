    package com.streamit.groupchatapp.dto;

    import lombok.*;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class UserRequestDTO {
        private Long id;
        private String email;
        private String name;
        private String profileImageUrl;
    }
