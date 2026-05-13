package com.cromp.executions.domain.model.exceptions;

public class ExecutionAttemptNotFoundException extends RuntimeException {
    public ExecutionAttemptNotFoundException(Long attemptId) {
        super("Execution attempt not found: " + attemptId);
    }
}
