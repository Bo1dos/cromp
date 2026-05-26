package com.cromp.analytics.domain.model;

import java.time.Instant;

import com.cromp.analytics.domain.model.enums.AnomalySeverity;

/**
 * Обнаруженная аномалия в метриках выполнения задачи.
 * Value object — результат анализа ML-сервиса.
 *
 * @param expectedMin нижняя граница ожидаемого диапазона
 * @param expectedMax верхняя граница ожидаемого диапазона
 */
public record AnomalyDetection(
        Long jobId,
        String metric,          // например: "duration_ms", "error_rate"
        double value,           // фактическое значение метрики
        double expectedMin,
        double expectedMax,
        AnomalySeverity severity,
        Instant detectedAt
) {}