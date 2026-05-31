package com.cromp.jobs.infrastructure.persistence.mapper;

import com.cromp.jobs.domain.model.Job;
import com.cromp.jobs.domain.model.enums.JobStatus;
import com.cromp.jobs.infrastructure.persistence.jpa.entity.JobJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JobPersistenceMapperTest {

    private final JobPersistenceMapper mapper = new JobPersistenceMapper();

    @Test
    void shouldMapDomainToEntityAndBack() {
        Job job = Job.reconstitute(1L, UUID.randomUUID(), 11L, "Daily sync", "desc", JobStatus.DISABLED,
                "default", 7, 42L, Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T01:00:00Z"), Instant.parse("2024-01-01T02:00:00Z"));

        JobJpaEntity entity = mapper.toJpa(job);
        Job roundTripped = mapper.toDomain(entity);

        assertThat(entity.getName()).isEqualTo("Daily sync");
        assertThat(roundTripped.getId()).isEqualTo(1L);
        assertThat(roundTripped.getStatus()).isEqualTo(JobStatus.DISABLED);
        assertThat(roundTripped.getDeletedAt()).isEqualTo(Instant.parse("2024-01-01T02:00:00Z"));
    }
}
