package com.cromp.executions.api.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ExecutionDetailResponse(
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
        Instant updatedAt,
        List<AttemptResponse> attempts
) {}
