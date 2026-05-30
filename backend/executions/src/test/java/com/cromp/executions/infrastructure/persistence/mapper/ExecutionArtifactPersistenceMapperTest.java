package com.cromp.executions.infrastructure.persistence.mapper;

import com.cromp.executions.domain.model.ExecutionArtifact;
import com.cromp.executions.domain.model.enums.ArtifactKind;
import com.cromp.executions.infrastructure.persistence.jpa.entity.ExecutionArtifactJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionArtifactPersistenceMapperTest {

    private final ExecutionArtifactPersistenceMapper mapper = new ExecutionArtifactPersistenceMapper();

    @Test
    void shouldMapArtifactRoundTripWithoutLosingFields() {
        ExecutionArtifact artifact = ExecutionArtifact.reconstitute(
                1L, 10L, ArtifactKind.DEBUG_SNAPSHOT, "executions/10/debug.zip", 512L, "checksum",
                "application/zip", "gzip", "{\"a\":1}", 30, 99L, Instant.parse("2024-01-01T00:00:00Z")
        );

        ExecutionArtifactJpaEntity entity = mapper.toJpa(artifact);
        ExecutionArtifact restored = mapper.toDomain(entity);

        assertThat(entity.getKind()).isEqualTo(ArtifactKind.DEBUG_SNAPSHOT);
        assertThat(restored.getStoragePath()).isEqualTo("executions/10/debug.zip");
        assertThat(restored.getUploadedAt()).isEqualTo(artifact.getUploadedAt());
    }
}
