package com.streamit.groupchatapp.security.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ExchangeTokenRequest(
        @NotBlank String code
) {}