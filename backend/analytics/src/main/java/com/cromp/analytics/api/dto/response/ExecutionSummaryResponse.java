package com.cromp.analytics.api.dto.response;

import java.time.Instant;

public record ExecutionSummaryResponse(
        Long organizationId,
        Long jobId,
        Instant from,
        Instant to,
        long totalExecutions,
        long succeeded,
        long failed,
        long cancelled,
        long skipped,
        double successRate,
        Double avgDurationMs,
        Double p95DurationMs
) {}
