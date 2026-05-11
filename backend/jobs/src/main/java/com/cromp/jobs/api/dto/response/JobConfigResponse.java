package com.cromp.jobs.api.dto.response;

import java.util.List;
import java.util.Map;

public record JobConfigResponse(
        JobTargetResponse target,
        RetryPolicyResponse retryPolicy,
        int timeoutMs,
        List<SecretRefResponse> secrets
) {}