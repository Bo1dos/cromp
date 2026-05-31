package com.cromp.executions.domain.model;

import com.cromp.executions.domain.model.enums.ArtifactKind;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExecutionArtifactTest {

    @Test
    void shouldCreateArtifactWithUploadedAt() {
        Instant before = Instant.now();

        ExecutionArtifact artifact = ExecutionArtifact.create(
                10L, ArtifactKind.LOG_STDOUT, "executions/10/stdout.log",
                128L, "abc", "text/plain", 99L
        );

        assertThat(artifact.getExecutionId()).isEqualTo(10L);
        assertThat(artifact.getKind()).isEqualTo(ArtifactKind.LOG_STDOUT);
        assertThat(artifact.getStoragePath()).isEqualTo("executions/10/stdout.log");
        assertThat(artifact.getUploadedBy()).isEqualTo(99L);
        assertThat(artifact.getUploadedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void shouldReconstituteArtifactWithAllFields() {
        Instant uploadedAt = Instant.parse("2024-01-01T00:00:00Z");

        ExecutionArtifact artifact = ExecutionArtifact.reconstitute(
                1L, 10L, ArtifactKind.OUTPUT_PAYLOAD, "executions/10/output.json",
                512L, "checksum", "application/json", "gzip", "{\"k\":true}", 30,
                99L, uploadedAt
        );

        assertThat(artifact.getId()).isEqualTo(1L);
        assertThat(artifact.getKind()).isEqualTo(ArtifactKind.OUTPUT_PAYLOAD);
        assertThat(artifact.getCompression()).isEqualTo("gzip");
        assertThat(artifact.getMetadata()).isEqualTo("{\"k\":true}");
        assertThat(artifact.getUploadedAt()).isEqualTo(uploadedAt);
    }

    @Test
    void shouldThrowWhenRequiredFieldsAreMissing() {
        assertThatThrownBy(() -> ExecutionArtifact.create(null, ArtifactKind.LOG_STDERR, "path", 1L, "c", "t", 1L))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ExecutionArtifact.create(1L, null, "path", 1L, "c", "t", 1L))
                .isInstanceOf(NullPointerException.class);
    }
}
