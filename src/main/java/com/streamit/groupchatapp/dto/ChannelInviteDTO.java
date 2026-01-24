package com.streamit.groupchatapp.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChannelInviteDTO {
    private Long inviteId;
    private Long channelId;
    private String channelName;
    private Long invitedById;
    private String invitedByName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
