package com.cromp.executions.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record UpdateAttemptStatusRequest(
        @NotNull String status,
        String statusReason,
        String errorClass,
        Map<String, Object> outputSummary
) {}
