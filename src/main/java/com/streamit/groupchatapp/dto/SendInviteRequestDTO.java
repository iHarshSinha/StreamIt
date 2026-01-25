package com.streamit.groupchatapp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SendInviteRequestDTO {

    @NotNull(message = "Channel id is required")
    @Min(value = 1, message = "Channel id must be greater than 0")
    private Long channelId;

    @NotNull(message = "Invited user id is required")
    @Min(value = 1, message = "Invited user id must be greater than 0")
    private Long invitedUserId;
}