package com.cromp.executions.infrastructure.port;

import com.cromp.executions.application.port.StaleAttemptPort;
import com.cromp.executions.application.service.AttemptService;
import com.cromp.executions.domain.model.ExecutionAttempt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Реализация {@link StaleAttemptPort}.
 *
 * Живёт в infrastructure-слое модуля executions — знает о {@link AttemptService}
 */
@Component
@RequiredArgsConstructor
public class StaleAttemptPortImpl implements StaleAttemptPort {

    private final AttemptService attemptService;

    @Override
    public List<ExecutionAttempt> findStaleAttempts(int olderThanMinutes) {
        return attemptService.findStaleAttempts(olderThanMinutes);
    }

    @Override
    public void markStaleAsTimeout(List<ExecutionAttempt> stale) {
        attemptService.markStaleAsTimeout(stale);
    }
}