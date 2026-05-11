package com.cromp.jobs.api.dto.request;

import java.util.Map;
import java.util.UUID;

public record TriggerJobRequest(
        String payloadOverride,
        UUID correlationId,
        Map<String, Object> parameters
) {}