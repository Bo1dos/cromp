package com.cromp.analytics.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * DTO ответов API аналитики.
 * Все record'ы — неизменяемые, сериализуются Jackson'ом.
 * {@code @JsonInclude(NON_NULL)} — не включаем null-поля в JSON.
 */
public final class AnalyticsDtos {

    private AnalyticsDtos() {}

    // ── Summary ───────────────────────────────────────────────────────────────

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SummaryResponse(
            Long organizationId,
            Long jobId,             // null если запрос по всей организации
            String period,
            long total,
            long succeeded,
            long failed,
            double errorRate,
            Double avgDurationMs,
            Double p95DurationMs,
            Instant calculatedAt
    ) {}

    // ── Predictions ───────────────────────────────────────────────────────────

    public record PredictionsResponse(
            Long organizationId,
            Long jobId,
            List<PredictionItem> predictions
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PredictionItem(
            Long jobId,
            Instant nextRunAt,
            double failureProbability,
            Long expectedDurationMs,
            double confidence
    ) {}

    // ── Anomalies ─────────────────────────────────────────────────────────────

    public record AnomaliesResponse(
            Long organizationId,
            Long jobId,
            String from,
            String to,
            List<AnomalyItem> anomalies
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AnomalyItem(
            Long jobId,
            String metric,
            double value,
            double expectedMin,
            double expectedMax,
            String severity,
            Instant detectedAt
    ) {}
}