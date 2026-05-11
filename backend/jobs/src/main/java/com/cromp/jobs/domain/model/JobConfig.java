package com.cromp.jobs.domain.model;

import java.util.List;

public record JobConfig(
    JobTarget target,
    RetryPolicy retryPolicy,
    int timeoutMs,
    List<JobSecretRef> secrets
) {
    public JobConfig {
        if (target == null) throw new IllegalArgumentException("target must not be null");
        if (timeoutMs < 0) throw new IllegalArgumentException("timeoutMs must be >= 0");
        retryPolicy = retryPolicy != null ? retryPolicy : RetryPolicy.defaultPolicy();
        secrets = secrets == null ? List.of() : List.copyOf(secrets);
    }
}