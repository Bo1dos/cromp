package com.cromp.jobs.infrastructure.persistence.mapper;

import com.cromp.jobs.domain.model.*;
import com.cromp.jobs.infrastructure.persistence.jpa.entity.JobVersionJpaEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JobVersionPersistenceMapperTest {

    private final JobVersionPersistenceMapper mapper = new JobVersionPersistenceMapper(new ObjectMapper());

    @Test
    void shouldMapDomainToJsonEntityAndBack() {
        JobVersion version = JobVersion.reconstitute(1L, 10L, 2, sampleConfig(), 42L,
                Instant.parse("2024-01-01T00:00:00Z"));

        JobVersionJpaEntity entity = mapper.toJpa(version);
        JobVersion roundTripped = mapper.toDomain(entity);

        assertThat(entity.getConfig()).contains("\"timeoutMs\":1000");
        assertThat(roundTripped.getId()).isEqualTo(1L);
        assertThat(roundTripped.getConfig()).isEqualTo(version.getConfig());
        assertThat(roundTripped.getCreatedAt()).isEqualTo(version.getCreatedAt());
    }

    private static JobConfig sampleConfig() {
        return new JobConfig(
                JobTarget.forHttp("https://example.com", "POST", Map.of("X-Trace", "1"), "payload"),
                RetryPolicy.defaultPolicy(),
                1_000,
                List.of(new JobSecretRef(UUID.randomUUID(), "API_KEY"))
        );
    }
}
