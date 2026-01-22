package com.streamit.groupchatapp.mapper;

import com.streamit.groupchatapp.dto.ChannelRequestDTO;
import com.streamit.groupchatapp.dto.ChannelResponseDTO;
import com.streamit.groupchatapp.dto.ChannelResponseDetailedDTO;
import com.streamit.groupchatapp.model.Channel;

public class ChannelMapper {

    // Request DTO -> Entity (for creating a new Channel)
    public static Channel toEntity(ChannelRequestDTO dto) {
        if (dto == null) return null;

        return Channel.builder()
                .channelName(dto.getChannelName())
                .channelDescription(dto.getChannelDescription())
                .type(dto.getType())
                .build();
        // createdOn is handled automatically by @PrePersist
    }

    // Entity -> Response DTO (normal list response)
    public static ChannelResponseDTO toResponse(Channel channel) {
        if (channel == null) return null;

        return ChannelResponseDTO.builder()
                .id(channel.getId())
                .channelName(channel.getChannelName())
                .channelDescription(channel.getChannelDescription())
                .createdOn(channel.getCreatedOn())
                .type(channel.getType())
                .build();
    }

    // Entity -> Response Detailed DTO (currently empty DTO class, so return minimal info)
    public static ChannelResponseDetailedDTO toDetailedResponse(Channel channel) {
        if (channel == null)
        {
            System.out.println("Hey! DB returned null!");
            return null;
        }

        return ChannelResponseDetailedDTO.builder()
                .id(channel.getId())
                .channelName(channel.getChannelName())
                .channelDescription(channel.getChannelDescription())
                .createdOn(channel.getCreatedOn())
                .type(channel.getType())
                .build();
    }

    // Update an existing entity using Request DTO (recommended for PATCH/PUT style update)
    public static void updateEntity(Channel channel, ChannelRequestDTO dto) {
        if (channel == null || dto == null) return;

        if (dto.getChannelName() != null) {
            channel.setChannelName(dto.getChannelName());
        }

        if (dto.getChannelDescription() != null) {
            channel.setChannelDescription(dto.getChannelDescription());
        }

        if (dto.getType() != null) {
            channel.setType(dto.getType());
        }
    }
}
