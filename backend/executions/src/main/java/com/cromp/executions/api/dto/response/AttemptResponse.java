package com.cromp.executions.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AttemptResponse(
        UUID attemptUuid,
        int attemptNumber,
        String status,
        String statusReason,
        String errorClass,
        Instant scheduledAt,
        Instant claimedAt,
        Instant startedAt,
        Instant finishedAt,
        Integer durationMs,
        UUID traceId,
        Instant createdAt,
        Instant updatedAt
) {}
