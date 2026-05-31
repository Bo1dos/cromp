package com.cromp.schedules.infrastructure.persistence.jpa.repository;

import com.cromp.schedules.domain.model.enums.ScheduleStatus;
import com.cromp.schedules.infrastructure.persistence.SchedulesPersistenceTestConfiguration;
import com.cromp.schedules.infrastructure.persistence.SchedulesPostgresTestSupport;
import com.cromp.schedules.infrastructure.persistence.jpa.entity.ScheduleJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
@Import(SchedulesPersistenceTestConfiguration.class)
class ScheduleJpaRepositoryIntegrationTest extends SchedulesPostgresTestSupport {

    @Autowired
    private ScheduleJpaRepository repository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void saveShouldPersistAndFindByJobIdShouldReturnTheSameRow() {
        Long jobId = seedJob();

        ScheduleJpaEntity saved = repository.save(schedule(jobId, ScheduleStatus.ACTIVE, "{\"enabled\":true}",
                Instant.parse("2024-01-01T02:00:00Z")));
        repository.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findByJobId(jobId)).isPresent();
        ScheduleJpaEntity loaded = repository.findByJobId(jobId).orElseThrow();
        assertThat(loaded.getJobId()).isEqualTo(jobId);
        assertThat(loaded.getStatus()).isEqualTo(ScheduleStatus.ACTIVE);
        assertThat(loaded.getRules()).isEqualTo("{\"enabled\":true}");
        assertThat(loaded.getCreatedAt()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
        assertThat(loaded.getUpdatedAt()).isEqualTo(Instant.parse("2024-01-01T01:00:00Z"));
    }

    @Test
    void shouldAllowNullAndEmptyRulesValues() {
        Long jobIdNull = seedJob();
        Long jobIdEmpty = seedJob();

        repository.save(schedule(jobIdNull, ScheduleStatus.ACTIVE, null, Instant.parse("2024-01-01T02:00:00Z")));
        repository.save(schedule(jobIdEmpty, ScheduleStatus.PAUSED, "", Instant.parse("2024-01-01T03:00:00Z")));
        repository.flush();

        assertThat(repository.findByJobId(jobIdNull).orElseThrow().getRules()).isNull();
        assertThat(repository.findByJobId(jobIdEmpty).orElseThrow().getRules()).isEqualTo("");
    }

    @Test
    void shouldEnforceUniqueJobIdConstraint() {
        Long jobId = seedJob();
        repository.save(schedule(jobId, ScheduleStatus.ACTIVE, null, Instant.parse("2024-01-01T02:00:00Z")));
        repository.flush();

        assertThatThrownBy(() -> {
            repository.save(schedule(jobId, ScheduleStatus.PAUSED, null, Instant.parse("2024-01-01T03:00:00Z")));
            repository.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    void shouldPersistStatusRulesAndTimestampsAsProvided() {
        Long jobId = seedJob();
        ScheduleJpaEntity saved = repository.save(schedule(jobId, ScheduleStatus.PAUSED, "{\"mode\":\"manual\"}",
                Instant.parse("2024-01-01T04:00:00Z")));
        repository.flush();

        assertThat(saved.getStatus()).isEqualTo(ScheduleStatus.PAUSED);
        assertThat(repository.findByJobId(jobId).orElseThrow().getStatus()).isEqualTo(ScheduleStatus.PAUSED);
    }

    private Long seedJob() {
        Long organizationId = jdbcTemplate.queryForObject(
                "insert into organizations (org_uuid, name, settings, created_at, updated_at) values (?, ?, '{}'::jsonb, ?, ?) returning id",
                Long.class,
                UUID.randomUUID(), "Acme", Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z")
        );
        return jdbcTemplate.queryForObject(
                "insert into jobs (job_uuid, organization_id, name, description, status, queue_name, priority, created_by, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) returning id",
                Long.class,
                UUID.randomUUID(), organizationId, "job-" + UUID.randomUUID(), "desc", "ACTIVE", "default", 1, null,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z")
        );
    }

    private static ScheduleJpaEntity schedule(Long jobId, ScheduleStatus status, String rules, Instant nextRunAt) {
        return ScheduleJpaEntity.builder()
                .jobId(jobId)
                .cronExpression("*/5 * * * *")
                .timezone("Europe/Moscow")
                .rules(rules)
                .nextRunAt(nextRunAt)
                .status(status)
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2024-01-01T01:00:00Z"))
                .build();
    }
}
