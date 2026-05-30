package com.cromp.executions.domain.model;

import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.enums.ExecutionStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExecutionTest {

    @Test
    void shouldCreateExecutionWithCreatedStatusAndGeneratedUuid() {
        Instant before = Instant.now();

        Execution execution = Execution.create(
                10L, 20L, 30L, 5, ExecutionSource.SCHEDULED,
                null, UUID.randomUUID(), "{\"retry\":true}"
        );

        assertThat(execution.getExecUuid()).isNotNull();
        assertThat(execution.getOrganizationId()).isEqualTo(10L);
        assertThat(execution.getJobId()).isEqualTo(20L);
        assertThat(execution.getJobVersionId()).isEqualTo(30L);
        assertThat(execution.getPriority()).isEqualTo(5);
        assertThat(execution.getSource()).isEqualTo(ExecutionSource.SCHEDULED);
        assertThat(execution.getFinalStatus()).isEqualTo(ExecutionStatus.CREATED);
        assertThat(execution.getTotalAttempts()).isZero();
        assertThat(execution.getScheduledAt()).isNull();
        assertThat(execution.getTriggeredAt()).isAfterOrEqualTo(before);
        assertThat(execution.getCreatedAt()).isAfterOrEqualTo(before);
        assertThat(execution.getUpdatedAt()).isAfterOrEqualTo(before);
        assertThat(execution.getExecutionPolicySnapshot()).isEqualTo("{\"retry\":true}");
    }

    @Test
    void shouldReconstituteExecutionWithAllFields() {
        UUID execUuid = UUID.randomUUID();
        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2024-01-01T00:01:00Z");
        Instant triggeredAt = Instant.parse("2024-01-01T00:02:00Z");

        Execution execution = Execution.reconstitute(
                1L, execUuid, 10L, 20L, 30L, 7, ExecutionSource.API,
                triggeredAt, Instant.parse("2024-01-01T00:03:00Z"), ExecutionStatus.IN_PROGRESS,
                2, Instant.parse("2024-01-01T00:04:00Z"), null, UUID.randomUUID(),
                "{\"k\":1}", createdAt, updatedAt
        );

        assertThat(execution.getId()).isEqualTo(1L);
        assertThat(execution.getExecUuid()).isEqualTo(execUuid);
        assertThat(execution.getTriggeredAt()).isEqualTo(triggeredAt);
        assertThat(execution.getFinalStatus()).isEqualTo(ExecutionStatus.IN_PROGRESS);
        assertThat(execution.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void shouldMarkExecutionInProgressAndSetStartedAtWhenMissing() {
        Execution execution = Execution.create(10L, 20L, 30L, 0, ExecutionSource.MANUAL, null, null, null);

        execution.markInProgress();

        assertThat(execution.getFinalStatus()).isEqualTo(ExecutionStatus.IN_PROGRESS);
        assertThat(execution.getStartedAt()).isNotNull();
        assertThat(execution.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldCompleteExecutionAndSetFinishedAt() {
        Execution execution = Execution.create(10L, 20L, 30L, 0, ExecutionSource.API, null, null, null);

        execution.complete(ExecutionStatus.SUCCEEDED);

        assertThat(execution.getFinalStatus()).isEqualTo(ExecutionStatus.SUCCEEDED);
        assertThat(execution.getFinishedAt()).isNotNull();
        assertThat(execution.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldCancelExecutionAndSetCancelledStatus() {
        Execution execution = Execution.create(10L, 20L, 30L, 0, ExecutionSource.API, null, null, null);

        execution.cancel();

        assertThat(execution.getFinalStatus()).isEqualTo(ExecutionStatus.CANCELLED);
        assertThat(execution.getFinishedAt()).isNotNull();
    }

    @Test
    void shouldThrowWhenRequiredFieldsAreMissing() {
        assertThatThrownBy(() -> Execution.create(null, 20L, 30L, 0, ExecutionSource.API, null, null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Execution.create(10L, null, 30L, 0, ExecutionSource.API, null, null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Execution.create(10L, 20L, null, 0, ExecutionSource.API, null, null, null))
                .isInstanceOf(NullPointerException.class);
    }
}
