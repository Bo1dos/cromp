package com.cromp.executions.infrastructure.persistence.custom;

import com.cromp.executions.api.dto.response.ClaimAttemptResult;
import com.cromp.executions.infrastructure.persistence.ExecutionsPostgresTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Concurrent attempt claim")
class ConcurrentClaimTest extends ExecutionsPostgresTestSupport {

    @Autowired
    private ExecutionAttemptCustomRepository customRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("two concurrent claimants should get different attempts when two available")
    void twoClaimantsShouldGetDifferentAttempts() throws Exception {
        Long orgId = seedOrg();
        Long jobId = seedJob(orgId);
        Long jobVersionId = seedJobVersion(jobId);
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        Long execId = seedExec(orgId, jobId, jobVersionId, now);
        seedAttempt(execId, orgId, 1, now);
        seedAttempt(execId, orgId, 2, now);

        int threads = 2;
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<Optional<ClaimAttemptResult>>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                latch.await();
                return customRepository.claimAttempt(orgId);
            }));
        }
        latch.countDown();

        List<Optional<ClaimAttemptResult>> results = new ArrayList<>();
        for (Future<Optional<ClaimAttemptResult>> f : futures) {
            results.add(f.get());
        }
        pool.shutdown();

        long claimedCount = results.stream().filter(Optional::isPresent).count();
        assertThat(claimedCount).isEqualTo(2);

        List<UUID> uuids = results.stream()
                .filter(Optional::isPresent)
                .map(r -> r.get().attemptUuid())
                .distinct()
                .toList();
        assertThat(uuids).hasSize(2);
    }

    @Test
    @DisplayName("single attempt claimed by only one of two concurrent threads")
    void singleAttemptClaimedByOne() throws Exception {
        Long orgId = seedOrg();
        Long jobId = seedJob(orgId);
        Long jobVersionId = seedJobVersion(jobId);
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        Long execId = seedExec(orgId, jobId, jobVersionId, now);
        seedAttempt(execId, orgId, 1, now);

        int threads = 2;
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<Optional<ClaimAttemptResult>>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                latch.await();
                return customRepository.claimAttempt(orgId);
            }));
        }
        latch.countDown();

        List<Optional<ClaimAttemptResult>> results = new ArrayList<>();
        for (Future<Optional<ClaimAttemptResult>> f : futures) {
            results.add(f.get());
        }
        pool.shutdown();

        long claimedCount = results.stream().filter(Optional::isPresent).count();
        assertThat(claimedCount).isEqualTo(1);
    }

    private Long seedOrg() {
        return jdbcTemplate.queryForObject(
                "insert into organizations (org_uuid, name, settings, created_at, updated_at) values (?, ?, '{}'::jsonb, ?, ?) returning id",
                Long.class, UUID.randomUUID(), "o-" + UUID.randomUUID(),
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"));
    }

    private Long seedJob(Long orgId) {
        return jdbcTemplate.queryForObject(
                "insert into jobs (job_uuid, organization_id, name, description, status, queue_name, priority, created_at, updated_at) values (?, ?, ?, 'x', 'ACTIVE', 'default', 1, ?, ?) returning id",
                Long.class, UUID.randomUUID(), orgId, "j-" + UUID.randomUUID(),
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"));
    }

    private Long seedJobVersion(Long jobId) {
        return jdbcTemplate.queryForObject(
                "insert into job_versions (job_id, version, lock_version, config, created_at) values (?, ?, 0, ?::jsonb, ?) returning id",
                Long.class, jobId, 1, "{\"claim\":true}", Instant.parse("2024-01-01T00:00:00Z"));
    }

    private Long seedExec(Long orgId, Long jobId, Long jvId, Instant now) {
        return jdbcTemplate.queryForObject(
                "insert into executions (exec_uuid, organization_id, job_id, job_version_id, priority, source, triggered_at, scheduled_at, final_status, total_attempts, correlation_id, execution_policy_snapshot, created_at, updated_at) values (?, ?, ?, ?, 5, 'SCHEDULED', ?, ?, 'CREATED', 0, ?, '{}'::jsonb, ?, ?) returning id",
                Long.class, UUID.randomUUID(), orgId, jobId, jvId, now, now, UUID.randomUUID(), now, now);
    }

    private void seedAttempt(Long execId, Long orgId, int n, Instant now) {
        jdbcTemplate.update(
                "insert into execution_attempts (attempt_uuid, execution_id, organization_id, attempt_number, status, scheduled_at, idempotency_key, created_at, updated_at) values (?, ?, ?, ?, 'PENDING', ?, ?, ?, ?)",
                UUID.randomUUID(), execId, orgId, n, now, UUID.randomUUID(), now, now);
    }
}
