package com.cromp.executions.infrastructure.persistence.adapter;

import com.cromp.executions.domain.model.ExecutionArtifact;
import com.cromp.executions.domain.model.enums.ArtifactKind;
import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.infrastructure.persistence.ExecutionsPostgresTestSupport;
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
class ExecutionArtifactRepositoryAdapterTest extends ExecutionsPostgresTestSupport {

    @Autowired private ExecutionArtifactRepositoryAdapter adapter;
    @Autowired private ExecutionRepositoryAdapter executionRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void shouldSaveAndLoadArtifactsOrderedByUploadedAt() {
        Long orgId = seedOrganization();
        Long jobId = seedJob(orgId);
        Long jobVersionId = seedJobVersion(jobId);
        var execution = executionRepository.save(com.cromp.executions.domain.model.Execution.create(orgId, jobId, jobVersionId, 5, ExecutionSource.API, null, UUID.randomUUID(), null));

        adapter.save(ExecutionArtifact.create(execution.getId(), ArtifactKind.LOG_STDOUT, "executions/1/a", 1L, "c", "text/plain", 11L));
        ExecutionArtifact later = adapter.save(ExecutionArtifact.create(execution.getId(), ArtifactKind.LOG_STDERR, "executions/1/b", 1L, "d", "text/plain", 11L));

        assertThat(adapter.findById(later.getId())).isPresent();
        assertThat(adapter.findByExecutionId(execution.getId()))
                .extracting(ExecutionArtifact::getStoragePath)
                .contains("executions/1/a", "executions/1/b");
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
}
