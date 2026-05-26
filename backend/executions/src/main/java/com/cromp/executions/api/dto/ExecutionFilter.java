package com.cromp.executions.api.dto;

import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.enums.ExecutionStatus;

import java.time.Instant;

public record ExecutionFilter(
        Long jobId,
        ExecutionStatus status,
        ExecutionSource source,
        Instant from,
        Instant to,
        int page,
        int size
) {}
