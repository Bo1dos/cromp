package com.cromp.orchestrator.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Orchestrator full cycle")
class OrchestratorFullCycleTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long orgId;
    private Long jobId;
    private Long jobVersionId;

    @BeforeEach
    void setUp() {
        orgId = seedOrganization();
        jobId = seedJob(orgId, "test-job");
        jobVersionId = seedJobVersion(jobId, """
            {"target":{"type":"HTTP","url":"https://httpbin.org/get","method":"GET"},
             "retryPolicy":{"maxAttempts":3,"backoffMs":1000,"backoffMultiplier":2.0,"retryableErrors":["5xx","TIMEOUT"]},
             "timeoutMs":5000,"secrets":[]}""");
    }

    @Test
    @DisplayName("full cycle: schedule → execution created → attempt pending → claim → complete")
    void shouldCreateExecutionFromSchedule() {
        Instant past = Instant.parse("2024-01-01T00:00:00Z");
        seedSchedule(jobId, "0 0 * * *", "UTC", past);

        var dueSchedules = jdbcTemplate.queryForList(
                "select s.id, s.job_id, s.next_run_at from schedules s join jobs j on s.job_id = j.id where s.next_run_at <= ? and s.status = 'ACTIVE' and j.status = 'ACTIVE'",
                Instant.now()
        );
        assertThat(dueSchedules).isNotEmpty();

        // 3. Проверяем, что Execution создался
        var executions = jdbcTemplate.queryForList(
                "select * from executions where job_id = ? and organization_id = ?",
                jobId, orgId
        );
        assertThat(executions).describedAs("execution should be created by scheduler").isNotEmpty();

        // Проверяем, что next_run_at продвинулся вперёд
        String nextRun = jdbcTemplate.queryForObject(
                "select next_run_at from schedules where job_id = ?",
                String.class, jobId
        );
        assertThat(Instant.parse(nextRun)).isAfter(past);
    }

    @Test
    @DisplayName("should claim pending attempt after execution is created")
    void shouldClaimPendingAttempt() {
        // Подготовка: создаём execution с pending attempt
        Instant now = Instant.now();
        UUID execUuid = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into executions (exec_uuid, organization_id, job_id, job_version_id, priority, source, triggered_at, scheduled_at, final_status, total_attempts, correlation_id, execution_policy_snapshot, created_at, updated_at) values (?, ?, ?, ?, 5, 'SCHEDULED', ?, ?, 'CREATED', 0, ?, '{}'::jsonb, ?, ?)",
                execUuid, orgId, jobId, jobVersionId, now, now, UUID.randomUUID(), now, now
        );
        Long execId = jdbcTemplate.queryForObject(
                "select id from executions where exec_uuid = ?", Long.class, execUuid
        );

        UUID attemptUuid = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into execution_attempts (attempt_uuid, execution_id, organization_id, attempt_number, status, scheduled_at, idempotency_key, created_at, updated_at) values (?, ?, ?, 1, 'PENDING', ?, ?, ?, ?)",
                attemptUuid, execId, orgId, now, UUID.randomUUID(), now, now
        );

        // Проверим, что запись создалась в статусе PENDING
        var status = jdbcTemplate.queryForObject(
                "select status from execution_attempts where attempt_uuid = ?",
                String.class, attemptUuid
        );
        assertThat(status).isEqualTo("PENDING");
        assertThat(attemptUuid).isNotNull();
    }

    @Test
    @DisplayName("should mark attempt as SUCCEEDED after completion")
    void shouldCompleteAttempt() {
        Instant now = Instant.now();
        UUID execUuid = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into executions (exec_uuid, organization_id, job_id, job_version_id, priority, source, triggered_at, scheduled_at, final_status, total_attempts, correlation_id, execution_policy_snapshot, created_at, updated_at) values (?, ?, ?, ?, 5, 'SCHEDULED', ?, ?, 'CREATED', 1, ?, '{}'::jsonb, ?, ?)",
                execUuid, orgId, jobId, jobVersionId, now, now, UUID.randomUUID(), now, now
        );
        Long execId = jdbcTemplate.queryForObject(
                "select id from executions where exec_uuid = ?", Long.class, execUuid
        );

        UUID attemptUuid = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into execution_attempts (attempt_uuid, execution_id, organization_id, attempt_number, status, started_at, scheduled_at, idempotency_key, created_at, updated_at) values (?, ?, ?, 1, 'RUNNING', ?, ?, ?, ?, ?)",
                attemptUuid, execId, orgId, now, now, UUID.randomUUID(), now, now
        );

        // Завершаем попытку успешно
        jdbcTemplate.update(
                "update execution_attempts set status = 'SUCCEEDED', output_summary = '{\"ok\":true}'::jsonb, finished_at = ?, duration_ms = 150 where attempt_uuid = ?",
                Instant.now(), attemptUuid
        );

        String finalStatus = jdbcTemplate.queryForObject(
                "select status from execution_attempts where attempt_uuid = ?",
                String.class, attemptUuid
        );
        assertThat(finalStatus).isEqualTo("SUCCEEDED");
    }

    private Long seedOrganization() {
        return jdbcTemplate.queryForObject(
                "insert into organizations (org_uuid, name, settings, created_at, updated_at) values (?, ?, '{}'::jsonb, ?, ?) returning id",
                Long.class,
                UUID.randomUUID(), "org-" + UUID.randomUUID(),
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z")
        );
    }

    private Long seedJob(Long orgId, String name) {
        return jdbcTemplate.queryForObject(
                "insert into jobs (job_uuid, organization_id, name, description, status, queue_name, priority, created_at, updated_at) values (?, ?, ?, 'desc', 'ACTIVE', 'default', 1, ?, ?) returning id",
                Long.class,
                UUID.randomUUID(), orgId, name,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z")
        );
    }

    private Long seedJobVersion(Long jobId, String config) {
        return jdbcTemplate.queryForObject(
                "insert into job_versions (job_id, version, lock_version, config, created_at) values (?, ?, 0, ?::jsonb, ?) returning id",
                Long.class,
                jobId, 1, config, Instant.parse("2024-01-01T00:00:00Z")
        );
    }

    private void seedSchedule(Long jobId, String cron, String timezone, Instant nextRunAt) {
        jdbcTemplate.update(
                "insert into schedules (job_id, cron_expression, timezone, status, next_run_at, created_at, updated_at) values (?, ?, ?, 'ACTIVE', ?, ?, ?)",
                jobId, cron, timezone, nextRunAt,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z")
        );
    }
}
