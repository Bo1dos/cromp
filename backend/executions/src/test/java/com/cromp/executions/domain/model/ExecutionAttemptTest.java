package com.cromp.executions.domain.model;

import com.cromp.executions.domain.model.enums.AttemptStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExecutionAttemptTest {

    @Test
    void shouldCreateAttemptWithPendingStatusAndGeneratedIdempotencyKey() {
        Instant before = Instant.now();

        ExecutionAttempt attempt = ExecutionAttempt.create(1L, 2L, 1, Instant.parse("2024-01-01T00:00:00Z"), null);

        assertThat(attempt.getAttemptUuid()).isNotNull();
        assertThat(attempt.getExecutionId()).isEqualTo(1L);
        assertThat(attempt.getOrganizationId()).isEqualTo(2L);
        assertThat(attempt.getAttemptNumber()).isEqualTo(1);
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.PENDING);
        assertThat(attempt.getIdempotencyKey()).isNotNull();
        assertThat(attempt.getCreatedAt()).isAfterOrEqualTo(before);
        assertThat(attempt.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void shouldReconstituteAttemptWithAllFields() {
        UUID attemptUuid = UUID.randomUUID();
        UUID idempotencyKey = UUID.randomUUID();
        UUID traceId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2024-01-01T00:01:00Z");

        ExecutionAttempt attempt = ExecutionAttempt.reconstitute(
                1L, attemptUuid, 2L, 3L, 4, AttemptStatus.RUNNING, "reason", "error",
                Instant.parse("2024-01-01T00:05:00Z"), Instant.parse("2024-01-01T00:06:00Z"),
                Instant.parse("2024-01-01T00:07:00Z"), null, 10, "{\"meta\":true}", idempotencyKey,
                "{\"out\":1}", traceId, createdAt, updatedAt
        );

        assertThat(attempt.getId()).isEqualTo(1L);
        assertThat(attempt.getAttemptUuid()).isEqualTo(attemptUuid);
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.RUNNING);
        assertThat(attempt.getTraceId()).isEqualTo(traceId);
        assertThat(attempt.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void shouldDispatchStartCompleteCancelAndTimeout() {
        ExecutionAttempt attempt = ExecutionAttempt.create(1L, 2L, 1, null, null);

        attempt.dispatch();
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.DISPATCHED);
        assertThat(attempt.getClaimedAt()).isNotNull();

        attempt.start(UUID.randomUUID());
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.RUNNING);
        assertThat(attempt.getStartedAt()).isNotNull();
        assertThat(attempt.getTraceId()).isNotNull();

        attempt.complete(AttemptStatus.SUCCEEDED, "{\"ok\":true}", null, "done");
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.SUCCEEDED);
        assertThat(attempt.getOutputSummary()).isEqualTo("{\"ok\":true}");
        assertThat(attempt.getStatusReason()).isEqualTo("done");
        assertThat(attempt.getFinishedAt()).isNotNull();
        assertThat(attempt.getDurationMs()).isNotNull();

        attempt.cancel("cancelled");
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.CANCELLED);
        assertThat(attempt.getStatusReason()).isEqualTo("cancelled");

        attempt.markTimeout();
        assertThat(attempt.getStatus()).isEqualTo(AttemptStatus.TIMEOUT);
        assertThat(attempt.getStatusReason()).isEqualTo("Timed out by janitor");
    }

    @Test
    void shouldCalculateDurationWhenStartedAtExists() {
        ExecutionAttempt attempt = ExecutionAttempt.reconstitute(
                1L, UUID.randomUUID(), 2L, 3L, 1, AttemptStatus.RUNNING, null, null,
                null, null, Instant.now().minusMillis(50), null, null, null,
                UUID.randomUUID(), null, null, Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z")
        );

        attempt.complete(AttemptStatus.FAILED, null, "java.lang.RuntimeException", "boom");

        assertThat(attempt.getDurationMs()).isNotNull();
        assertThat(attempt.getDurationMs()).isBetween(0, 1_000);
    }

    @Test
    void shouldThrowWhenRequiredFieldsAreMissing() {
        assertThatThrownBy(() -> ExecutionAttempt.create(null, 2L, 1, null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ExecutionAttempt.create(1L, null, 1, null, null))
                .isInstanceOf(NullPointerException.class);
    }
}
