package com.cromp.orchestrator.executor;

import java.util.Map;

public record ExecutionResult(
        boolean succeeded,
        String statusReason,
        String errorClass,
        Map<String, Object> outputSummary
) {
    public static ExecutionResult success(Map<String, Object> outputSummary) {
        return new ExecutionResult(true, "HTTP target completed", null, outputSummary);
    }

    public static ExecutionResult failure(String reason, String errorClass, Map<String, Object> outputSummary) {
        return new ExecutionResult(false, reason, errorClass, outputSummary);
    }
}
