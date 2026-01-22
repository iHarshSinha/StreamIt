package com.streamit.groupchatapp.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ViewerMembershipDTO {
    private boolean isMember;
    private String role;       // "ADMIN"/"MEMBER"
    private String status;     // "ACTIVE"/"BANNED"/...
    private
    LocalDateTime joinedAt;
}
