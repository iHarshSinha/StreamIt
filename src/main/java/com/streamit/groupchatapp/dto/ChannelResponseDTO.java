package com.streamit.groupchatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChannelResponseDTO {
    private Long id;
    private String channelName;
    private String channelDescription;
    private LocalDateTime createdOn;
    private String type;
}
