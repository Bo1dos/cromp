package com.cromp.jobs.domain.model;

import java.util.List;

import static com.cromp.jobs.domain.model.support.SchemaLimits.*;

public record RetryPolicy(
    int maxAttempts,
    long backoffMs,
    double backoffMultiplier,
    List<String> retryableErrors
) {
    public RetryPolicy {
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
        if (backoffMs < 0) throw new IllegalArgumentException("backoffMs must be >= 0");
        if (backoffMultiplier < 1.0) throw new IllegalArgumentException("backoffMultiplier must be >= 1.0");
        retryableErrors = retryableErrors == null ? List.of() : List.copyOf(retryableErrors);
    }

    public static RetryPolicy defaultPolicy() {
        return new RetryPolicy(DEFAULT_MAX_ATTEMPTS, DEFAULT_BACKOFF_MS, DEFAULT_BACKOFF_MULTIPLIER, List.of("5xx", "TIMEOUT", "NETWORK_ERROR"));
    }
}
