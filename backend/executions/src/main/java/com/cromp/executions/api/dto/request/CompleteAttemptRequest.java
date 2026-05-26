package com.cromp.executions.api.dto.request;

import com.cromp.executions.domain.model.enums.AttemptStatus;

public record CompleteAttemptRequest(
        AttemptStatus status,       // SUCCEEDED | FAILED | TIMEOUT | CANCELLED
        String outputSummary,       // JSON-строка, nullable
        String errorClass,          // nullable
        String statusReason         // nullable
) {}
