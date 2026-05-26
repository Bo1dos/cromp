package com.cromp.executions.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ExecutionResponse(
        UUID execUuid,
        Long jobId,
        Long jobVersionId,
        int priority,
        String source,
        String finalStatus,
        int totalAttempts,
        Instant triggeredAt,
        Instant scheduledAt,
        Instant startedAt,
        Instant finishedAt,
        UUID correlationId,
        Instant createdAt,
        Instant updatedAt
) {}
