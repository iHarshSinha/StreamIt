package com.streamit.groupchatapp.dto;

import lombok.Getter;

@Getter
public class SendInviteRequestDTO {
    private Long channelId;
    private Long invitedUserId;
}
