package com.streamit.groupchatapp.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ViewerMembershipDTO {

    // ask ibrahim but this is what i think
    // when user asks for all the message this tells about the person who sent the request.
    private boolean isMember;
    private String role;       // "ADMIN"/"MEMBER"
    private String status;     // "ACTIVE"/"BANNED"/...
    private LocalDateTime joinedAt;
}
