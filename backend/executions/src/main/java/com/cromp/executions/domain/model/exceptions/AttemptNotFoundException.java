package com.cromp.executions.domain.model.exceptions;

import java.util.UUID;

public class AttemptNotFoundException extends RuntimeException {
    public AttemptNotFoundException(UUID attemptUuid) {
        super("ExecutionAttempt not found: " + attemptUuid);
    }
}