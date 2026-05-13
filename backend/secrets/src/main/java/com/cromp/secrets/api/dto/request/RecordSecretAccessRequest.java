package com.cromp.secrets.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RecordSecretAccessRequest(
        @NotNull Long attemptId,
        @NotNull Long secretVersionId,
        @NotBlank String accessedKey
) {}
