package com.cromp.executions.infrastructure.persistence.jpa.repository;

import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.enums.ExecutionStatus;
import com.cromp.executions.infrastructure.persistence.ExecutionsPostgresTestSupport;
import com.cromp.executions.infrastructure.persistence.jpa.entity.ExecutionJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class ExecutionJpaRepositoryTest extends ExecutionsPostgresTestSupport {

    @Autowired private ExecutionJpaRepository repository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void shouldFindByExecUuidAndRoundTripJsonbFields() {
        Long organizationId = seedOrganization();
        Long jobId = seedJob(organizationId);
        Long jobVersionId = seedJobVersion(jobId);
        UUID execUuid = UUID.randomUUID();

        repository.save(executionEntity(organizationId, jobId, jobVersionId, execUuid,
                ExecutionStatus.CREATED, ExecutionSource.API,
                Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:01:00Z")));

        assertThat(repository.findByExecUuid(execUuid)).isPresent();
        assertThat(repository.findByExecUuid(execUuid).orElseThrow().getExecutionPolicySnapshot()).isEqualTo("{\"retry\":true}");
    }

    @Test
    void shouldFindAndCountByFilterWithCreatedAtSorting() {
        Long organizationId = seedOrganization();
        Long jobId = seedJob(organizationId);
        Long jobVersionId = seedJobVersion(jobId);
        repository.save(executionEntity(organizationId, jobId, jobVersionId, UUID.randomUUID(),
                ExecutionStatus.CREATED, ExecutionSource.API,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z")));
        repository.save(executionEntity(organizationId, jobId, jobVersionId, UUID.randomUUID(),
                ExecutionStatus.IN_PROGRESS, ExecutionSource.MANUAL,
                Instant.parse("2024-01-02T00:00:00Z"), Instant.parse("2024-01-02T00:00:00Z")));
        repository.save(executionEntity(seedOrganization(), jobId, jobVersionId, UUID.randomUUID(),
                ExecutionStatus.CREATED, ExecutionSource.API,
                Instant.parse("2024-01-03T00:00:00Z"), Instant.parse("2024-01-03T00:00:00Z")));

        List<ExecutionJpaEntity> results = repository.findByFilter(
                organizationId, jobId, null, null,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-03T00:00:00Z"),
                org.springframework.data.domain.PageRequest.of(0, 10)
        );

        assertThat(results).hasSize(2);
        assertThat(results).extracting(ExecutionJpaEntity::getCreatedAt)
                .containsExactly(
                        Instant.parse("2024-01-02T00:00:00Z"),
                        Instant.parse("2024-01-01T00:00:00Z")
                );
        assertThat(repository.countByFilter(
                organizationId, jobId, null, null,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-03T00:00:00Z")
        )).isEqualTo(2L);
    }

    @Test
    void shouldFilterByStatusAndSource() {
        Long organizationId = seedOrganization();
        Long jobId = seedJob(organizationId);
        Long jobVersionId = seedJobVersion(jobId);
        repository.save(executionEntity(organizationId, jobId, jobVersionId, UUID.randomUUID(),
                ExecutionStatus.CREATED, ExecutionSource.API,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z")));
        repository.save(executionEntity(organizationId, jobId, jobVersionId, UUID.randomUUID(),
                ExecutionStatus.FAILED, ExecutionSource.MANUAL,
                Instant.parse("2024-01-02T00:00:00Z"), Instant.parse("2024-01-02T00:00:00Z")));

        assertThat(repository.findByFilter(
                organizationId, jobId, ExecutionStatus.FAILED, ExecutionSource.MANUAL,
                null, null, org.springframework.data.domain.PageRequest.of(0, 10)
        )).hasSize(1);
    }

    private Long seedOrganization() {
        return jdbcTemplate.queryForObject(
                "insert into organizations (org_uuid, name, settings, created_at, updated_at) values (?, ?, '{}'::jsonb, ?, ?) returning id",
                Long.class,
                UUID.randomUUID(), "org-" + UUID.randomUUID(), Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z")
        );
    }

    private Long seedJob(Long organizationId) {
        return jdbcTemplate.queryForObject(
                "insert into jobs (job_uuid, organization_id, name, description, status, queue_name, priority, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?) returning id",
                Long.class,
                UUID.randomUUID(), organizationId, "job-" + UUID.randomUUID(), "desc",
                "ACTIVE", "default", 1,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z")
        );
    }

    private Long seedJobVersion(Long jobId) {
        return jdbcTemplate.queryForObject(
                "insert into job_versions (job_id, version, lock_version, config, created_at) values (?, ?, 0, ?::jsonb, ?) returning id",
                Long.class,
                jobId, 1, "{\"retry\":true}", Instant.parse("2024-01-01T00:00:00Z")
        );
    }

    private ExecutionJpaEntity executionEntity(Long organizationId, Long jobId, Long jobVersionId,
                                               UUID execUuid, ExecutionStatus status, ExecutionSource source,
                                               Instant createdAt, Instant updatedAt) {
        return ExecutionJpaEntity.builder()
                .execUuid(execUuid)
                .organizationId(organizationId)
                .jobId(jobId)
                .jobVersionId(jobVersionId)
                .priority(5)
                .source(source)
                .triggeredAt(createdAt)
                .scheduledAt(createdAt)
                .finalStatus(status)
                .totalAttempts(0)
                .correlationId(UUID.randomUUID())
                .executionPolicySnapshot("{\"retry\":true}")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
