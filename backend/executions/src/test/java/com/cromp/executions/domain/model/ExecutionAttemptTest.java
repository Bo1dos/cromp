package com.cromp.executions.domain.model;

import com.cromp.executions.domain.model.enums.AttemptStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionAttemptTest {
    @Test
    void terminalStatusSetsFinishedAtAndOutputSummary() {
        ExecutionAttempt attempt = ExecutionAttempt.create(1L, 10L, 1, Instant.now());

        attempt.changeStatus(AttemptStatus.SUCCEEDED, "ok", null, Map.of("httpStatus", 200));

        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.SUCCEEDED);
        assertThat(attempt.getFinishedAt()).isNotNull();
        assertThat(attempt.getOutputSummary()).containsEntry("httpStatus", 200);
    }
}
