package com.cromp.executions.api.dto.response;

import java.time.Instant;

public record ArtifactResponse(
        Long id,
        String kind,
        String storagePath,
        Long sizeBytes,
        String contentType,
        String checksumSha256,
        Integer retentionDays,
        Instant uploadedAt
) {}
