package com.cromp.executions.api.dto.request;

import com.cromp.executions.domain.model.enums.ExecutionSource;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CreateExecutionRequest(
        Long organizationId,
        Long jobId,
        Long jobVersionId,
        int priority,
        ExecutionSource source,
        Long triggeredBy,
        Instant scheduledAt,
        UUID correlationId,
        Map<String, Object> payload,
        String executionPolicySnapshot
) {}