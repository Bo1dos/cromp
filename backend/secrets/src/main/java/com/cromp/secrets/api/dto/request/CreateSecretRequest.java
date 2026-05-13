package com.cromp.secrets.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSecretRequest(
        @NotBlank String name,
        String description,
        @NotNull String scope,
        @NotBlank String value
) {}
