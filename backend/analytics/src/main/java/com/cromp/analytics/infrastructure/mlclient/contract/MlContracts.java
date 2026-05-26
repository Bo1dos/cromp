package com.cromp.analytics.infrastructure.mlclient.contract;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Контракты запросов и ответов ML-сервиса.
 * Все record'ы сериализуются в JSON через Jackson.
 */
public final class MlContracts {

    private MlContracts() {}

    // ── Predictions ───────────────────────────────────────────────────────────

    /**
     * Запрос к POST /predictions.
     * Передаём исторические данные по задаче и мета-информацию.
     */
    public record PredictionRequest(
            @JsonProperty("org_id")       Long orgId,
            @JsonProperty("job_id")       Long jobId,       // null — для всех задач организации
            @JsonProperty("history")      List<ExecutionRow> history
    ) {}

    /**
     * Одна строка исторических данных — соответствует одному дню из MV.
     */
    public record ExecutionRow(
            @JsonProperty("job_id")          Long jobId,
            @JsonProperty("day")             String day,
            @JsonProperty("total")           long total,
            @JsonProperty("succeeded")       long succeeded,
            @JsonProperty("failed")          long failed,
            @JsonProperty("avg_duration_ms") Double avgDurationMs,
            @JsonProperty("p95_duration_ms") Double p95DurationMs
    ) {}

    /**
     * Ответ от POST /predictions — список прогнозов.
     */
    public record PredictionResponse(
            @JsonProperty("predictions") List<PredictionItem> predictions
    ) {}

    public record PredictionItem(
            @JsonProperty("job_id")               Long jobId,
            @JsonProperty("next_run_at")          String nextRunAt,       // ISO-8601, может быть null
            @JsonProperty("failure_probability")  double failureProbability,
            @JsonProperty("expected_duration_ms") Long expectedDurationMs,
            @JsonProperty("confidence")           double confidence
    ) {}

    // ── Anomalies ─────────────────────────────────────────────────────────────

    /**
     * Запрос к POST /anomalies.
     */
    public record AnomalyRequest(
            @JsonProperty("org_id")   Long orgId,
            @JsonProperty("job_id")   Long jobId,
            @JsonProperty("from")     String from,     // ISO-8601 date
            @JsonProperty("to")       String to,
            @JsonProperty("history")  List<ExecutionRow> history
    ) {}

    /**
     * Ответ от POST /anomalies — список обнаруженных аномалий.
     */
    public record AnomalyResponse(
            @JsonProperty("anomalies") List<AnomalyItem> anomalies
    ) {}

    public record AnomalyItem(
            @JsonProperty("job_id")         Long jobId,
            @JsonProperty("metric")         String metric,
            @JsonProperty("value")          double value,
            @JsonProperty("expected_min")   double expectedMin,
            @JsonProperty("expected_max")   double expectedMax,
            @JsonProperty("severity")       String severity,    // LOW/MEDIUM/HIGH/CRITICAL
            @JsonProperty("detected_at")    String detectedAt   // ISO-8601
    ) {}
}