package com.streamit.groupchatapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelRequestDTO {

    @NotBlank(message = "Channel name is required")
    @Size(min = 3, max = 50, message = "Channel name must be between 3 and 50 characters")
    private String channelName;

    @NotBlank(message = "Channel description is required")
    @Size(max = 255, message = "Channel description must be at most 255 characters")
    private String channelDescription;

    @NotBlank(message = "Channel type is required")
    private String type;
}