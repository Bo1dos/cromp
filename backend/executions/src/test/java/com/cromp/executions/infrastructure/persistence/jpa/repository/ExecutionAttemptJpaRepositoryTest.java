package com.cromp.executions.infrastructure.persistence.jpa.repository;

import com.cromp.executions.domain.model.enums.AttemptStatus;
import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.enums.ExecutionStatus;
import com.cromp.executions.infrastructure.persistence.ExecutionsPostgresTestSupport;
import com.cromp.executions.infrastructure.persistence.jpa.entity.ExecutionAttemptJpaEntity;
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
class ExecutionAttemptJpaRepositoryTest extends ExecutionsPostgresTestSupport {

    @Autowired private ExecutionAttemptJpaRepository attemptRepository;
    @Autowired private ExecutionJpaRepository executionRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void shouldFindByAttemptUuidAndOrderByAttemptNumberAndMaxNumber() {
        Long orgId = seedOrganization();
        Long jobId = seedJob(orgId);
        Long jobVersionId = seedJobVersion(jobId);
        ExecutionJpaEntity execution = executionRepository.save(executionEntity(orgId, jobId, jobVersionId, UUID.randomUUID()));
        ExecutionAttemptJpaEntity first = attemptRepository.save(attemptEntity(execution.getId(), execution.getOrganizationId(), 1, AttemptStatus.PENDING, Instant.parse("2024-01-01T00:00:00Z")));
        attemptRepository.save(attemptEntity(execution.getId(), execution.getOrganizationId(), 3, AttemptStatus.FAILED, Instant.parse("2024-01-03T00:00:00Z")));
        ExecutionAttemptJpaEntity second = attemptRepository.save(attemptEntity(execution.getId(), execution.getOrganizationId(), 2, AttemptStatus.RUNNING, Instant.parse("2024-01-02T00:00:00Z")));

        assertThat(attemptRepository.findByAttemptUuid(first.getAttemptUuid())).isPresent();
        assertThat(attemptRepository.findByExecutionIdOrderByAttemptNumberAsc(execution.getId()))
                .extracting(ExecutionAttemptJpaEntity::getAttemptNumber)
                .containsExactly(1, 2, 3);
        assertThat(attemptRepository.findMaxAttemptNumberByExecutionId(execution.getId())).isEqualTo(3);
        assertThat(attemptRepository.findByStatusAndUpdatedAtBefore(AttemptStatus.RUNNING, Instant.parse("2024-01-03T00:00:00Z")))
                .extracting(ExecutionAttemptJpaEntity::getAttemptUuid)
                .containsExactly(second.getAttemptUuid());
    }

    @Test
    void shouldFindActiveAttemptsAndJoinByExecUuidAndOrgId() {
        Long orgId = seedOrganization();
        Long jobId = seedJob(orgId);
        Long jobVersionId = seedJobVersion(jobId);
        ExecutionJpaEntity execution = executionRepository.save(executionEntity(orgId, jobId, jobVersionId, UUID.randomUUID()));
        attemptRepository.save(attemptEntity(execution.getId(), orgId, 1, AttemptStatus.PENDING, Instant.parse("2024-01-01T00:00:00Z")));
        attemptRepository.save(attemptEntity(execution.getId(), orgId, 2, AttemptStatus.RUNNING, Instant.parse("2024-01-02T00:00:00Z")));
        attemptRepository.save(attemptEntity(execution.getId(), orgId, 3, AttemptStatus.SUCCEEDED, Instant.parse("2024-01-03T00:00:00Z")));

        assertThat(attemptRepository.findActiveByExecutionId(execution.getId()))
                .extracting(ExecutionAttemptJpaEntity::getAttemptNumber)
                .containsExactly(1, 2);
        assertThat(attemptRepository.findByExecUuidAndOrgId(execution.getExecUuid(), orgId))
                .extracting(ExecutionAttemptJpaEntity::getAttemptNumber)
                .containsExactly(1, 2, 3);
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

    private ExecutionJpaEntity executionEntity(Long organizationId, Long jobId, Long jobVersionId, UUID execUuid) {
        return ExecutionJpaEntity.builder()
                .execUuid(execUuid)
                .organizationId(organizationId)
                .jobId(jobId)
                .jobVersionId(jobVersionId)
                .priority(5)
                .source(ExecutionSource.API)
                .triggeredAt(Instant.parse("2024-01-01T00:00:00Z"))
                .scheduledAt(Instant.parse("2024-01-01T00:00:00Z"))
                .finalStatus(ExecutionStatus.CREATED)
                .totalAttempts(0)
                .correlationId(UUID.randomUUID())
                .executionPolicySnapshot("{\"retry\":true}")
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2024-01-01T00:00:00Z"))
                .build();
    }

    private ExecutionAttemptJpaEntity attemptEntity(Long executionId, Long organizationId, int number,
                                                    AttemptStatus status, Instant updatedAt) {
        return ExecutionAttemptJpaEntity.builder()
                .attemptUuid(UUID.randomUUID())
                .executionId(executionId)
                .organizationId(organizationId)
                .attemptNumber(number)
                .status(status)
                .scheduledAt(Instant.parse("2024-01-01T00:00:00Z"))
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .updatedAt(updatedAt)
                .build();
    }
}
