package com.cromp.analytics.api.dto.response;

public record PredictionResponse(
        Long jobId,
        double failureProbability,
        Double expectedDurationMs,
        String confidence,
        String recommendation
) {}
