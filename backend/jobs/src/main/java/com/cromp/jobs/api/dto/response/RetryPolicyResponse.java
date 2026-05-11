package com.cromp.jobs.api.dto.response;

import java.util.List;

public record RetryPolicyResponse(
        int maxAttempts,
        long backoffMs,
        double backoffMultiplier,
        List<String> retryableErrors
) {}