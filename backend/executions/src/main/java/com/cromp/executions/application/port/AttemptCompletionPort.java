package com.cromp.executions.application.port;

import com.cromp.executions.api.dto.request.CompleteAttemptRequest;

import java.util.UUID;

// Вызывается executor'ом: отметить начало выполнения и зафиксировать результат
public interface AttemptCompletionPort {
    void markRunning(UUID attemptUuid);
    void completeAttempt(UUID attemptUuid, CompleteAttemptRequest request);
}