package com.cromp.executions.application.port;

import com.cromp.executions.api.dto.request.CompleteAttemptRequest;

import java.util.UUID;

// Вызывается executor'ом: зафиксировать результат попытки
public interface AttemptCompletionPort {
    void completeAttempt(UUID attemptUuid, CompleteAttemptRequest request);
}