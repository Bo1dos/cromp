package com.cromp.executions.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CreateExecutionArtifactRequest(
        @NotNull String kind,
        @NotBlank String storagePath,
        Long sizeBytes,
        String checksumSha256,
        String contentType,
        String compression,
        Map<String, Object> metadata,
        Integer retentionDays
) {}
