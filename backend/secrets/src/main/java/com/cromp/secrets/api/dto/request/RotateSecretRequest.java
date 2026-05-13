package com.cromp.secrets.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RotateSecretRequest(
        @NotBlank String value
) {}