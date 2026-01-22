package com.streamit.groupchatapp.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelRequestDTO {
    private String channelName;
    private String channelDescription;
    private String type;
}
