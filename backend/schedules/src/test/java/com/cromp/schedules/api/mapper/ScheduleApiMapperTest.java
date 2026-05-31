package com.cromp.schedules.api.mapper;

import com.cromp.schedules.domain.model.Schedule;
import com.cromp.schedules.domain.model.enums.ScheduleStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleApiMapperTest {

    @Test
    void toResponseShouldMapAllFields() {
        Schedule schedule = Schedule.reconstitute(1L, 10L, "*/5 * * * *", "Europe/Moscow", "{\"enabled\":true}",
                Instant.parse("2024-01-01T02:00:00Z"), ScheduleStatus.ACTIVE,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T01:00:00Z"), null);

        var response = new ScheduleApiMapper().toResponse(schedule);

        assertThat(response.cronExpression()).isEqualTo("*/5 * * * *");
        assertThat(response.timezone()).isEqualTo("Europe/Moscow");
        assertThat(response.rules()).isEqualTo("{\"enabled\":true}");
        assertThat(response.nextRunAt()).isEqualTo(Instant.parse("2024-01-01T02:00:00Z"));
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
        assertThat(response.updatedAt()).isEqualTo(Instant.parse("2024-01-01T01:00:00Z"));
    }
}
