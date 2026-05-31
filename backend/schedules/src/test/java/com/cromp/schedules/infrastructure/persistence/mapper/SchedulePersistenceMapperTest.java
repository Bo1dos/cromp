package com.cromp.schedules.infrastructure.persistence.mapper;

import com.cromp.schedules.domain.model.Schedule;
import com.cromp.schedules.domain.model.enums.ScheduleStatus;
import com.cromp.schedules.infrastructure.persistence.jpa.entity.ScheduleJpaEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulePersistenceMapperTest {

    private final SchedulePersistenceMapper mapper = new SchedulePersistenceMapper();

    @Test
    void toJpaShouldMapAllFields() {
        Schedule schedule = Schedule.reconstitute(1L, 10L, "*/5 * * * *", "Europe/Moscow", "{\"x\":1}",
                Instant.parse("2024-01-01T02:00:00Z"), ScheduleStatus.PAUSED,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T01:00:00Z"), null);

        ScheduleJpaEntity entity = mapper.toJpa(schedule);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getJobId()).isEqualTo(10L);
        assertThat(entity.getCronExpression()).isEqualTo("*/5 * * * *");
        assertThat(entity.getTimezone()).isEqualTo("Europe/Moscow");
        assertThat(entity.getRules()).isEqualTo("{\"x\":1}");
        assertThat(entity.getNextRunAt()).isEqualTo(Instant.parse("2024-01-01T02:00:00Z"));
        assertThat(entity.getStatus()).isEqualTo(ScheduleStatus.PAUSED);
        assertThat(entity.getCreatedAt()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
        assertThat(entity.getUpdatedAt()).isEqualTo(Instant.parse("2024-01-01T01:00:00Z"));
    }

    @Test
    void toDomainShouldMapAllFieldsIncludingRulesAndStatus() {
        ScheduleJpaEntity entity = ScheduleJpaEntity.builder()
                .id(1L)
                .jobId(10L)
                .cronExpression("*/5 * * * *")
                .timezone("Europe/Moscow")
                .rules("{\"x\":1}")
                .nextRunAt(Instant.parse("2024-01-01T02:00:00Z"))
                .status(ScheduleStatus.ACTIVE)
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2024-01-01T01:00:00Z"))
                .build();

        Schedule schedule = mapper.toDomain(entity);

        assertThat(schedule.getId()).isEqualTo(1L);
        assertThat(schedule.getJobId()).isEqualTo(10L);
        assertThat(schedule.getRules()).isEqualTo("{\"x\":1}");
        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.ACTIVE);
        assertThat(schedule.getCreatedAt()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
        assertThat(schedule.getUpdatedAt()).isEqualTo(Instant.parse("2024-01-01T01:00:00Z"));
    }
}
