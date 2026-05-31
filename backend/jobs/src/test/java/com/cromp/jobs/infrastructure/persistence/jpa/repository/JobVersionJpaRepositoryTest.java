package com.cromp.jobs.infrastructure.persistence.jpa.repository;

import com.cromp.jobs.infrastructure.persistence.JobsPostgresTestSupport;
import com.cromp.jobs.infrastructure.persistence.jpa.entity.JobVersionJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class JobVersionJpaRepositoryTest extends JobsPostgresTestSupport {

    @Autowired private JobVersionJpaRepository repository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void shouldFindByJobAndOrderDescending() {
        Long jobId = seedJob();
        saveVersion(jobId, 1);
        saveVersion(jobId, 3);
        saveVersion(jobId, 2);

        List<JobVersionJpaEntity> versions = repository.findByJobIdOrderByVersionDesc(jobId);

        assertThat(versions).extracting(JobVersionJpaEntity::getVersion).containsExactly(3, 2, 1);
        assertThat(repository.findTopByJobIdOrderByVersionDesc(jobId)).get().extracting(JobVersionJpaEntity::getVersion).isEqualTo(3);
    }

    @Test
    void shouldFindVersionByJobAndVersion() {
        Long jobId = seedJob();
        JobVersionJpaEntity saved = saveVersion(jobId, 7);

        assertThat(repository.findByJobIdAndVersion(jobId, 7)).get().extracting(JobVersionJpaEntity::getId)
                .isEqualTo(saved.getId());
    }

    private Long seedJob() {
        Long organizationId = jdbcTemplate.queryForObject(
                "insert into organizations (org_uuid, name, settings, created_at, updated_at) values (?, ?, '{}'::jsonb, ?, ?) returning id",
                Long.class,
                java.util.UUID.randomUUID(), "Acme", Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z")
        );
        return jdbcTemplate.queryForObject(
                "insert into jobs (job_uuid, organization_id, name, status, queue_name, priority, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?) returning id",
                Long.class,
                java.util.UUID.randomUUID(), organizationId, "job", "ACTIVE", "default", 1,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z")
        );
    }

    private JobVersionJpaEntity saveVersion(Long jobId, int version) {
        return repository.save(JobVersionJpaEntity.builder()
                .jobId(jobId)
                .version(version)
                .lockVersion(0L)
                .config("{\"target\":{\"type\":\"HTTP\",\"url\":\"https://example.com\",\"method\":\"POST\",\"headers\":{},\"body\":\"\"},\"retryPolicy\":{\"maxAttempts\":3,\"backoffMs\":1000,\"backoffMultiplier\":2.0,\"retryableErrors\":[]},\"timeoutMs\":1000,\"secrets\":[]}")
                .createdBy(11L)
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .build());
    }
}
