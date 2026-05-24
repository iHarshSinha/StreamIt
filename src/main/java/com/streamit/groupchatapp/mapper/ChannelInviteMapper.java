package com.streamit.groupchatapp.mapper;

import com.streamit.groupchatapp.dto.ChannelInviteDTO;
import com.streamit.groupchatapp.model.ChannelInvite;
import org.springframework.stereotype.Component;

@Component
public class ChannelInviteMapper {

    public ChannelInviteDTO toDTO(ChannelInvite invite) {

        if (invite == null) {
            return null;
        }

        return ChannelInviteDTO.builder()
                .inviteId(invite.getId())
                .channelId(invite.getChannel().getId())
                .channelName(invite.getChannel().getChannelName())
                .invitedById(invite.getInvitedBy().getId())
                .invitedByName(invite.getInvitedBy().getName())
                .status(invite.getStatus().name())
                .createdAt(invite.getCreatedAt())
                .expiresAt(invite.getExpiresAt())
                .build();
    }
}