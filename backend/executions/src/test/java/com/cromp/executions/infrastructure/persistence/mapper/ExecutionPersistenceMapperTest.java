package com.cromp.executions.infrastructure.persistence.mapper;

import com.cromp.executions.domain.model.Execution;
import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.enums.ExecutionStatus;
import com.cromp.executions.infrastructure.persistence.jpa.entity.ExecutionJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionPersistenceMapperTest {

    private final ExecutionPersistenceMapper mapper = new ExecutionPersistenceMapper();

    @Test
    void shouldMapExecutionRoundTripWithoutLosingFields() {
        Execution execution = Execution.reconstitute(
                1L, UUID.randomUUID(), 10L, 20L, 30L, 5, ExecutionSource.API,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:05:00Z"),
                ExecutionStatus.IN_PROGRESS, 2, Instant.parse("2024-01-01T00:01:00Z"),
                Instant.parse("2024-01-01T00:10:00Z"), UUID.randomUUID(), "{\"retry\":true}",
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:06:00Z")
        );

        ExecutionJpaEntity entity = mapper.toJpa(execution);
        Execution restored = mapper.toDomain(entity);

        assertThat(entity.getExecUuid()).isEqualTo(execution.getExecUuid());
        assertThat(restored.getExecUuid()).isEqualTo(execution.getExecUuid());
        assertThat(restored.getExecutionPolicySnapshot()).isEqualTo("{\"retry\":true}");
        assertThat(restored.getFinalStatus()).isEqualTo(ExecutionStatus.IN_PROGRESS);
    }
}
