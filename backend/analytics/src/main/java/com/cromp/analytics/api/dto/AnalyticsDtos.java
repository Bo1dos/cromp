package com.cromp.analytics.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

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
    @Schema(description = "Сводка по выполнениям за период")
    public record SummaryResponse(
            @Schema(description = "ID организации")
            Long organizationId,
            @Schema(description = "ID задачи (null — сводка по всей организации)", nullable = true)
            Long jobId,
            @Schema(description = "Период: 1d, 7d, 30d, 90d")
            String period,
            @Schema(description = "Всего выполнений")
            long total,
            @Schema(description = "Успешных")
            long succeeded,
            @Schema(description = "С ошибкой")
            long failed,
            @Schema(description = "Процент ошибок (0.0 — 1.0)")
            double errorRate,
            @Schema(description = "Средняя длительность (мс)", nullable = true)
            Double avgDurationMs,
            @Schema(description = "95-й перцентиль длительности (мс)", nullable = true)
            Double p95DurationMs,
            @Schema(description = "Время расчёта сводки", nullable = true)
            Instant calculatedAt
    ) {}

    // ── Predictions ───────────────────────────────────────────────────────────

    @Schema(description = "Прогнозы по задачам")
    public record PredictionsResponse(
            @Schema(description = "ID организации")
            Long organizationId,
            @Schema(description = "ID задачи (null — все задачи)", nullable = true)
            Long jobId,
            @Schema(description = "Список прогнозов")
            List<PredictionItem> predictions
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Прогноз для одной задачи")
    public record PredictionItem(
            @Schema(description = "ID задачи")
            Long jobId,
            @Schema(description = "Ожидаемое время следующего запуска", nullable = true)
            Instant nextRunAt,
            @Schema(description = "Вероятность ошибки (0.0 — 1.0)")
            double failureProbability,
            @Schema(description = "Ожидаемая длительность (мс)", nullable = true)
            Long expectedDurationMs,
            @Schema(description = "Уверенность модели (0.0 — 1.0)")
            double confidence
    ) {}

    // ── Anomalies ─────────────────────────────────────────────────────────────

    @Schema(description = "Обнаруженные аномалии")
    public record AnomaliesResponse(
            @Schema(description = "ID организации")
            Long organizationId,
            @Schema(description = "ID задачи (null — все задачи)", nullable = true)
            Long jobId,
            @Schema(description = "Начало периода (ISO date)")
            String from,
            @Schema(description = "Конец периода (ISO date)")
            String to,
            @Schema(description = "Список аномалий")
            List<AnomalyItem> anomalies
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Одна обнаруженная аномалия")
    public record AnomalyItem(
            @Schema(description = "ID задачи")
            Long jobId,
            @Schema(description = "Метрика")
            String metric,
            @Schema(description = "Фактическое значение")
            double value,
            @Schema(description = "Нижняя граница ожидаемого")
            double expectedMin,
            @Schema(description = "Верхняя граница ожидаемого")
            double expectedMax,
            @Schema(description = "Серьёзность: LOW, MEDIUM, HIGH, CRITICAL")
            String severity,
            @Schema(description = "Время обнаружения")
            Instant detectedAt
    ) {}
}