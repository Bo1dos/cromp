package com.cromp.analytics.domain.model;

import java.time.Instant;

/**
 * Прогноз ML-сервиса для одной задачи.
 * Value object — результат инференса, не хранится в БД.
 */
public record Prediction(
        Long jobId,
        Instant nextRunAt,          // может быть null если ML не знает расписания
        double failureProbability,  // [0.0 .. 1.0]
        Long expectedDurationMs,    // null если модель не обучена на длительности
        double confidence           // уверенность модели [0.0 .. 1.0]
) {}