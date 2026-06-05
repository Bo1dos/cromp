package com.cromp.executions.infrastructure.port;

import com.cromp.executions.api.dto.request.CompleteAttemptRequest;
import com.cromp.executions.application.port.AttemptCompletionPort;
import com.cromp.executions.application.service.AttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttemptCompletionPortImpl implements AttemptCompletionPort {

    private final AttemptService attemptService;

    @Override
    public void markRunning(UUID attemptUuid) {
        attemptService.markRunning(attemptUuid);
    }

    @Override
    public void completeAttempt(UUID attemptUuid, CompleteAttemptRequest request) {
        attemptService.completeAttempt(attemptUuid, request);
    }
}