package com.cromp.analytics.domain.model;

import java.time.Instant;

/**
 * Сводная статистика выполнений за период.
 * Value object — неизменяемый, без идентификатора.
 * Формируется из материализованного представления {@code mv_job_execution_daily}.
 */
public record ExecutionSummary(
        Long organizationId,
        Long jobId,             // null — агрегат по всем задачам организации
        AnalyticsPeriod period,
        long total,
        long succeeded,
        long failed,
        double errorRate,       // failed / total, [0.0 .. 1.0]
        Double avgDurationMs,   // null если нет данных о длительности
        Double p95DurationMs,   // null если нет данных
        Instant calculatedAt
) {
    public ExecutionSummary {
        if (total > 0) {
            errorRate = (double) failed / total;
        } else {
            errorRate = 0.0;
        }
    }
}