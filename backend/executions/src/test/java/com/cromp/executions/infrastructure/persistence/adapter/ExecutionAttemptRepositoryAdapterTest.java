package com.cromp.executions.infrastructure.persistence.adapter;

import com.cromp.executions.domain.model.ExecutionAttempt;
import com.cromp.executions.domain.model.enums.AttemptStatus;
import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.enums.ExecutionStatus;
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
class ExecutionAttemptRepositoryAdapterTest extends ExecutionsPostgresTestSupport {

    @Autowired private ExecutionAttemptRepositoryAdapter adapter;
    @Autowired private ExecutionRepositoryAdapter executionRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void shouldSaveAndLoadAttemptsWithActiveQuery() {
        Long orgId = seedOrganization();
        Long jobId = seedJob(orgId);
        Long jobVersionId = seedJobVersion(jobId);
        var execution = executionRepository.save(com.cromp.executions.domain.model.Execution.create(orgId, jobId, jobVersionId, 5, ExecutionSource.API, null, UUID.randomUUID(), null));

        ExecutionAttempt pending = adapter.save(ExecutionAttempt.create(execution.getId(), orgId, 1, null, null));
        ExecutionAttempt running = adapter.save(ExecutionAttempt.create(execution.getId(), orgId, 2, null, null));
        running.start(UUID.randomUUID());
        adapter.save(running);
        ExecutionAttempt terminal = adapter.save(ExecutionAttempt.create(execution.getId(), orgId, 3, null, null));
        terminal.complete(AttemptStatus.SUCCEEDED, null, null, null);
        adapter.save(terminal);

        assertThat(adapter.findByAttemptUuid(pending.getAttemptUuid())).isPresent();
        assertThat(adapter.findByExecutionId(execution.getId()))
                .extracting(ExecutionAttempt::getAttemptNumber)
                .containsExactly(1, 2, 3);
        assertThat(adapter.findActiveByExecutionId(execution.getId()))
                .extracting(ExecutionAttempt::getAttemptNumber)
                .containsExactly(1, 2);
        assertThat(adapter.findMaxAttemptNumberByExecutionId(execution.getId())).isEqualTo(3);
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
