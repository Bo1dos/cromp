package com.cromp.jobs.api.dto.request;

import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

//TODO: перевести в константы
public record TriggerJobRequest(
        @Size(max = 8192) String payloadOverride,
        UUID correlationId,
        @Size(max = 100) Map<String, Object> parameters
) {}