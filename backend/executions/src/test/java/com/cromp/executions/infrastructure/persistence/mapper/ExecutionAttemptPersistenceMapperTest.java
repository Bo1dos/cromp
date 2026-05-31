package com.cromp.executions.infrastructure.persistence.mapper;

import com.cromp.executions.domain.model.ExecutionAttempt;
import com.cromp.executions.domain.model.enums.AttemptStatus;
import com.cromp.executions.infrastructure.persistence.jpa.entity.ExecutionAttemptJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionAttemptPersistenceMapperTest {

    private final ExecutionAttemptPersistenceMapper mapper = new ExecutionAttemptPersistenceMapper();

    @Test
    void shouldMapAttemptRoundTripWithoutLosingFields() {
        ExecutionAttempt attempt = ExecutionAttempt.reconstitute(
                1L, UUID.randomUUID(), 10L, 20L, 3, AttemptStatus.RUNNING, "reason", "error",
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:01:00Z"),
                Instant.parse("2024-01-01T00:02:00Z"), Instant.parse("2024-01-01T00:03:00Z"),
                60, "{\"meta\":true}", UUID.randomUUID(), "{\"ok\":true}", UUID.randomUUID(),
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:04:00Z")
        );

        ExecutionAttemptJpaEntity entity = mapper.toJpa(attempt);
        ExecutionAttempt restored = mapper.toDomain(entity);

        assertThat(entity.getAttemptUuid()).isEqualTo(attempt.getAttemptUuid());
        assertThat(restored.getAttemptUuid()).isEqualTo(attempt.getAttemptUuid());
        assertThat(restored.getDurationMs()).isEqualTo(60);
        assertThat(restored.getStatus()).isEqualTo(AttemptStatus.RUNNING);
    }
}
