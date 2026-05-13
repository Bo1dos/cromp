package com.cromp.executions.api.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ExecutionAttemptResponse(
        Long id,
        UUID attemptUuid,
        Long executionId,
        Long organizationId,
        int attemptNumber,
        String status,
        String statusReason,
        String errorClass,
        Instant scheduledAt,
        Instant claimedAt,
        Instant startedAt,
        Instant finishedAt,
        Integer durationMs,
        Map<String, Object> executorMetadata,
        UUID idempotencyKey,
        Map<String, Object> outputSummary,
        UUID traceId,
        Instant createdAt,
        Instant updatedAt
) {}
