package com.cromp.executions.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CreateExecutionRequest(
        @NotNull Long jobId,
        Long jobVersionId,
        Integer priority,
        @NotNull String source,
        Instant scheduledAt,
        UUID correlationId,
        Map<String, Object> executionPolicySnapshot
) {}
