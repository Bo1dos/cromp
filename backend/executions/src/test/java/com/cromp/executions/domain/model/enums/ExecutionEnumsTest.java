package com.cromp.executions.domain.model.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionEnumsTest {

    @Test
    void shouldContainExpectedStatusesAndKinds() {
        assertThat(ExecutionStatus.values()).containsExactly(
                ExecutionStatus.CREATED,
                ExecutionStatus.IN_PROGRESS,
                ExecutionStatus.SUCCEEDED,
                ExecutionStatus.FAILED,
                ExecutionStatus.CANCELLED,
                ExecutionStatus.SKIPPED
        );
        assertThat(ExecutionSource.values()).containsExactly(
                ExecutionSource.SCHEDULED,
                ExecutionSource.MANUAL,
                ExecutionSource.API
        );
        assertThat(AttemptStatus.values()).containsExactly(
                AttemptStatus.PENDING,
                AttemptStatus.DISPATCHED,
                AttemptStatus.RUNNING,
                AttemptStatus.SUCCEEDED,
                AttemptStatus.FAILED,
                AttemptStatus.TIMEOUT,
                AttemptStatus.CANCELLED,
                AttemptStatus.RETRYING
        );
        assertThat(ArtifactKind.values()).containsExactly(
                ArtifactKind.LOG_STDOUT,
                ArtifactKind.LOG_STDERR,
                ArtifactKind.OUTPUT_PAYLOAD,
                ArtifactKind.DEBUG_SNAPSHOT
        );
    }
}
