package com.streamit.groupchatapp.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageSenderDTO {
    private Long id;
    private String name;
    private String profileImageUrl;
}
