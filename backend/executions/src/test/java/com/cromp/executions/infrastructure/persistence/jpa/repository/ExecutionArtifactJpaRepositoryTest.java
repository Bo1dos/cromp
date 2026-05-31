package com.cromp.executions.infrastructure.persistence.jpa.repository;

import com.cromp.executions.domain.model.enums.ArtifactKind;
import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.enums.ExecutionStatus;
import com.cromp.executions.infrastructure.persistence.ExecutionsPostgresTestSupport;
import com.cromp.executions.infrastructure.persistence.jpa.entity.ExecutionArtifactJpaEntity;
import com.cromp.executions.infrastructure.persistence.jpa.entity.ExecutionJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class ExecutionArtifactJpaRepositoryTest extends ExecutionsPostgresTestSupport {

    @Autowired private ExecutionArtifactJpaRepository artifactRepository;
    @Autowired private ExecutionJpaRepository executionRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void shouldFindArtifactsOrderedByUploadedAtAscendingAndPersistJsonbFields() {
        Long orgId = seedOrganization();
        Long jobId = seedJob(orgId);
        Long jobVersionId = seedJobVersion(jobId);
        ExecutionJpaEntity execution = executionRepository.save(executionEntity(orgId, jobId, jobVersionId, UUID.randomUUID()));

        artifactRepository.save(artifactEntity(execution.getId(), ArtifactKind.LOG_STDOUT, "executions/1/a", Instant.parse("2024-01-02T00:00:00Z")));
        artifactRepository.save(artifactEntity(execution.getId(), ArtifactKind.LOG_STDERR, "executions/1/b", Instant.parse("2024-01-01T00:00:00Z")));

        assertThat(artifactRepository.findByExecutionIdOrderByUploadedAtAsc(execution.getId()))
                .extracting(ExecutionArtifactJpaEntity::getStoragePath)
                .containsExactly("executions/1/b", "executions/1/a");
        assertThat(artifactRepository.findByExecutionIdOrderByUploadedAtAsc(execution.getId()).getFirst().getKind())
                .isEqualTo(ArtifactKind.LOG_STDERR);
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

    private ExecutionArtifactJpaEntity artifactEntity(Long executionId, ArtifactKind kind, String path, Instant uploadedAt) {
        return ExecutionArtifactJpaEntity.builder()
                .executionId(executionId)
                .kind(kind)
                .storagePath(path)
                .sizeBytes(100L)
                .checksumSha256("checksum")
                .contentType("text/plain")
                .metadata("{\"a\":1}")
                .retentionDays(30)
                .uploadedBy(null)
                .uploadedAt(uploadedAt)
                .build();
    }
}
