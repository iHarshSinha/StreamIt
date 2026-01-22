package com.streamit.groupchatapp.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ChannelResponseDetailedDTO {
    private Long id;
    private String channelName;
    private String channelDescription;
    private LocalDateTime createdOn;
    private String type;

    private ViewerMembershipDTO viewer;
    private List<MessageDTO> messages;

    // for pagination / infinite scroll
    private Long nextCursor;
}
