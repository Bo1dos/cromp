package com.cromp.executions.domain.model.exceptions;

public class ExecutionNotFoundException extends RuntimeException {
    public ExecutionNotFoundException(Long executionId) {
        super("Execution not found: " + executionId);
    }
}
