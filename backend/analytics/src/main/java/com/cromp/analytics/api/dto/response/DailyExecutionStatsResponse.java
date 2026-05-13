package com.cromp.analytics.api.dto.response;

import java.time.LocalDate;

public record DailyExecutionStatsResponse(
        Long jobId,
        Long organizationId,
        LocalDate day,
        long totalExecutions,
        long succeeded,
        long failed,
        Double avgAttemptDurationMs,
        Double p50DurationMs,
        Double p95DurationMs,
        Integer maxDurationMs,
        long totalAttempts
) {}
