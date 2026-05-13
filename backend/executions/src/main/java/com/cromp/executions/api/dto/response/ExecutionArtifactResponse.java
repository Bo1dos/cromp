package com.cromp.executions.api.dto.response;

import java.time.Instant;
import java.util.Map;

public record ExecutionArtifactResponse(
        Long id,
        Long executionId,
        String kind,
        String storagePath,
        Long sizeBytes,
        String checksumSha256,
        String contentType,
        String compression,
        Map<String, Object> metadata,
        Integer retentionDays,
        Long uploadedBy,
        Instant uploadedAt
) {}
