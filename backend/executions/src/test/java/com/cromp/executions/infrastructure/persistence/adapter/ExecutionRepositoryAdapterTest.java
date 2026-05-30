package com.cromp.executions.infrastructure.persistence.adapter;

import com.cromp.executions.domain.model.Execution;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class ExecutionRepositoryAdapterTest extends ExecutionsPostgresTestSupport {

    @Autowired private ExecutionRepositoryAdapter adapter;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void shouldSaveAndFindExecutionWithFilterAndCount() {
        Long orgId = seedOrganization();
        Long jobId = seedJob(orgId);
        Long jobVersionId = seedJobVersion(jobId);
        Execution created = adapter.save(Execution.create(orgId, jobId, jobVersionId, 5, ExecutionSource.API, null, UUID.randomUUID(), "{\"retry\":true}"));
        adapter.save(Execution.create(orgId, jobId, jobVersionId, 6, ExecutionSource.MANUAL, null, UUID.randomUUID(), null));

        assertThat(adapter.findByExecUuid(created.getExecUuid())).isPresent();
        assertThat(adapter.findByFilter(orgId, jobId, ExecutionStatus.CREATED, ExecutionSource.API, null, null, 0, 10))
                .extracting(Execution::getExecUuid)
                .containsExactly(created.getExecUuid());
        assertThat(adapter.countByFilter(orgId, jobId, null, null, null, null)).isEqualTo(2L);
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
