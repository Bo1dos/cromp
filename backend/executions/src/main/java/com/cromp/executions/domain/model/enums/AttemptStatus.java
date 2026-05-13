package com.cromp.executions.domain.model.enums;

public enum AttemptStatus {
    PENDING,
    DISPATCHED,
    RUNNING,
    SUCCEEDED,
    FAILED,
    TIMEOUT,
    CANCELLED,
    RETRYING
}
