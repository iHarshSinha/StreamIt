package com.streamit.groupchatapp.dto;

import com.streamit.groupchatapp.dto.MessageSenderDTO;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MessageDTO {
    private Long id;
    private String content;
    private LocalDateTime sentAt;
    private MessageSenderDTO sender;
}
