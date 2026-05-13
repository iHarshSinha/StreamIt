package com.streamit.groupchatapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WsMessageRequest {
    private Long channelId;
    private String content;
}
