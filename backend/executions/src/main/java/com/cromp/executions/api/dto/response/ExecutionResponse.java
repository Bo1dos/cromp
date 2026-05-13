package com.cromp.executions.api.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ExecutionResponse(
        Long id,
        UUID execUuid,
        Long organizationId,
        Long jobId,
        Long jobVersionId,
        int priority,
        String source,
        Instant triggeredAt,
        Instant scheduledAt,
        String finalStatus,
        int totalAttempts,
        Instant startedAt,
        Instant finishedAt,
        UUID correlationId,
        Map<String, Object> executionPolicySnapshot,
        Instant createdAt,
        Instant updatedAt
) {}
