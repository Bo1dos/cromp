package com.cromp.executions.domain.model.exceptions;

import java.util.UUID;

public class ExecutionNotFoundException extends RuntimeException {
    public ExecutionNotFoundException(UUID execUuid) {
        super("Execution not found: " + execUuid);
    }
    public ExecutionNotFoundException(Long id) {
        super("Execution not found: id=" + id);
    }
}