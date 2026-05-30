package com.cromp.executions.api.mapper;

import com.cromp.executions.domain.model.Execution;
import com.cromp.executions.domain.model.ExecutionArtifact;
import com.cromp.executions.domain.model.ExecutionAttempt;
import com.cromp.executions.domain.model.enums.ArtifactKind;
import com.cromp.executions.domain.model.enums.AttemptStatus;
import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.enums.ExecutionStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionApiMapperTest {

    private final ExecutionApiMapper mapper = new ExecutionApiMapper();

    @Test
    void shouldMapExecutionToResponseWithoutLosingFields() {
        Execution execution = execution();

        var response = mapper.toResponse(execution);

        assertThat(response.execUuid()).isEqualTo(execution.getExecUuid());
        assertThat(response.jobId()).isEqualTo(execution.getJobId());
        assertThat(response.jobVersionId()).isEqualTo(execution.getJobVersionId());
        assertThat(response.source()).isEqualTo("MANUAL");
        assertThat(response.finalStatus()).isEqualTo("IN_PROGRESS");
        assertThat(response.correlationId()).isEqualTo(execution.getCorrelationId());
        assertThat(response.createdAt()).isEqualTo(execution.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(execution.getUpdatedAt());
    }

    @Test
    void shouldMapExecutionDetailResponseWithAttempts() {
        Execution execution = execution();
        ExecutionAttempt attempt = attempt();

        var response = mapper.toDetailResponse(execution, List.of(attempt));

        assertThat(response.execUuid()).isEqualTo(execution.getExecUuid());
        assertThat(response.attempts()).hasSize(1);
        assertThat(response.attempts().getFirst().attemptUuid()).isEqualTo(attempt.getAttemptUuid());
        assertThat(response.attempts().getFirst().status()).isEqualTo("RUNNING");
    }

    @Test
    void shouldMapAttemptToResponseWithoutLosingTimestamps() {
        ExecutionAttempt attempt = attempt();

        var response = mapper.toAttemptResponse(attempt);

        assertThat(response.attemptUuid()).isEqualTo(attempt.getAttemptUuid());
        assertThat(response.traceId()).isEqualTo(attempt.getTraceId());
        assertThat(response.createdAt()).isEqualTo(attempt.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(attempt.getUpdatedAt());
    }

    @Test
    void shouldMapArtifactToResponseWithoutLosingFields() {
        ExecutionArtifact artifact = ExecutionArtifact.reconstitute(
                1L, 10L, ArtifactKind.LOG_STDOUT, "executions/10/stdout.log",
                100L, "checksum", "text/plain", "gzip", "{\"a\":1}", 30, 11L,
                Instant.parse("2024-01-01T00:00:00Z")
        );

        var response = mapper.toArtifactResponse(artifact);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.kind()).isEqualTo("LOG_STDOUT");
        assertThat(response.storagePath()).isEqualTo("executions/10/stdout.log");
        assertThat(response.checksumSha256()).isEqualTo("checksum");
        assertThat(response.uploadedAt()).isEqualTo(artifact.getUploadedAt());
    }

    private static Execution execution() {
        return Execution.reconstitute(
                1L,
                UUID.randomUUID(),
                10L,
                20L,
                30L,
                5,
                ExecutionSource.MANUAL,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:05:00Z"),
                ExecutionStatus.IN_PROGRESS,
                2,
                Instant.parse("2024-01-01T00:01:00Z"),
                null,
                UUID.randomUUID(),
                "{\"policy\":true}",
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:06:00Z")
        );
    }

    private static ExecutionAttempt attempt() {
        return ExecutionAttempt.reconstitute(
                2L,
                UUID.randomUUID(),
                1L,
                10L,
                1,
                AttemptStatus.RUNNING,
                "reason",
                null,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:01:00Z"),
                Instant.parse("2024-01-01T00:02:00Z"),
                null,
                25,
                "{\"meta\":true}",
                UUID.randomUUID(),
                "{\"ok\":true}",
                UUID.randomUUID(),
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:03:00Z")
        );
    }
}
