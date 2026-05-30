package com.cromp.jobs.infrastructure.persistence.jpa.repository;

import com.cromp.jobs.domain.model.enums.JobStatus;
import com.cromp.jobs.infrastructure.persistence.JobsPostgresTestSupport;
import com.cromp.jobs.infrastructure.persistence.jpa.entity.JobJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class JobJpaRepositoryTest extends JobsPostgresTestSupport {

    @Autowired private JobJpaRepository repository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void shouldFindByUuidAndOrganizationAndFilterDeletedJobs() {
        Long organizationId = seedOrganization();
        JobJpaEntity active = saveJob(organizationId, "active", JobStatus.ACTIVE, null);
        saveJob(organizationId, "deleted", JobStatus.DISABLED, Instant.parse("2024-01-02T00:00:00Z"));

        assertThat(repository.findByJobUuidAndDeletedAtIsNull(active.getJobUuid())).isPresent();
        assertThat(repository.findByOrganizationIdAndDeletedAtIsNullOrderByCreatedAtDesc(organizationId))
                .extracting(JobJpaEntity::getName)
                .containsExactly("active");
    }

    @Test
    void shouldFindActiveJobsOnly() {
        Long organizationId = seedOrganization();
        saveJob(organizationId, "active", JobStatus.ACTIVE, null);
        saveJob(organizationId, "disabled", JobStatus.DISABLED, null);

        assertThat(repository.findByOrganizationIdAndStatusAndDeletedAtIsNull(organizationId, JobStatus.ACTIVE))
                .extracting(JobJpaEntity::getName)
                .containsExactly("active");
    }

    @Test
    void shouldEnforceUniqueNameWithinOrganizationForNonDeletedJobs() {
        Long organizationId = seedOrganization();
        saveJob(organizationId, "duplicate", JobStatus.ACTIVE, null);

        assertThatThrownBy(() -> {
            saveJob(organizationId, "duplicate", JobStatus.DISABLED, null);
            repository.flush();
        }).isInstanceOf(Exception.class);
    }

    private Long seedOrganization() {
        return jdbcTemplate.queryForObject(
                "insert into organizations (org_uuid, name, settings, created_at, updated_at) values (?, ?, '{}'::jsonb, ?, ?) returning id",
                Long.class,
                UUID.randomUUID(), "Acme", Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z")
        );
    }

    private JobJpaEntity saveJob(Long organizationId, String name, JobStatus status, Instant deletedAt) {
        return repository.save(JobJpaEntity.builder()
                .jobUuid(UUID.randomUUID())
                .organizationId(organizationId)
                .name(name)
                .description("description")
                .status(status)
                .queueName("default")
                .priority(1)
                .createdBy(null)
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2024-01-01T00:00:00Z"))
                .deletedAt(deletedAt)
                .build());
    }
}
