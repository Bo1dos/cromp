package com.cromp.analytics.api.dto.response;

import java.time.Instant;

public record AnomalyResponse(
        Long jobId,
        Long executionId,
        String severity,
        String type,
        String message,
        Instant detectedAt
) {}
