package com.cromp.schedules.domain.model;

import com.cromp.schedules.domain.model.enums.ScheduleStatus;
import com.cromp.schedules.domain.model.exceptions.InvalidScheduleStateException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScheduleTest {

    @Test
    void createShouldProduceActiveScheduleWithAllFieldsAndTimestamps() {
        Instant before = Instant.now();

        Schedule schedule = Schedule.create(10L, "*/5 * * * *", "  Europe/Moscow  ", "{\"enabled\":true}", Instant.parse("2024-01-01T01:00:00Z"));

        assertThat(schedule.getJobId()).isEqualTo(10L);
        assertThat(schedule.getCronExpression()).isEqualTo("*/5 * * * *");
        assertThat(schedule.getTimezone()).isEqualTo("Europe/Moscow");
        assertThat(schedule.getRules()).isEqualTo("{\"enabled\":true}");
        assertThat(schedule.getNextRunAt()).isEqualTo(Instant.parse("2024-01-01T01:00:00Z"));
        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.ACTIVE);
        assertThat(schedule.getCreatedAt()).isBetween(before, Instant.now());
        assertThat(schedule.getUpdatedAt()).isBetween(before, Instant.now());
        assertThat(schedule.getCreatedAt()).isEqualTo(schedule.getUpdatedAt());
    }

    @Test
    void reconstituteShouldRestoreStableState() {
        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2024-01-01T01:00:00Z");

        Schedule schedule = Schedule.reconstitute(1L, 10L, "*/5 * * * *", "Europe/Moscow", null,
                Instant.parse("2024-01-01T02:00:00Z"), ScheduleStatus.PAUSED, createdAt, updatedAt, null);

        assertThat(schedule.getId()).isEqualTo(1L);
        assertThat(schedule.getJobId()).isEqualTo(10L);
        assertThat(schedule.getCronExpression()).isEqualTo("*/5 * * * *");
        assertThat(schedule.getTimezone()).isEqualTo("Europe/Moscow");
        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.PAUSED);
        assertThat(schedule.getCreatedAt()).isEqualTo(createdAt);
        assertThat(schedule.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void updateCronShouldChangeCronNextRunAndUpdatedAt() throws InterruptedException {
        Schedule schedule = activeSchedule();
        Instant before = schedule.getUpdatedAt();

        Thread.sleep(2);
        schedule.updateCron("0 */2 * * *", Instant.parse("2024-01-01T03:00:00Z"));

        assertThat(schedule.getCronExpression()).isEqualTo("0 */2 * * *");
        assertThat(schedule.getNextRunAt()).isEqualTo(Instant.parse("2024-01-01T03:00:00Z"));
        assertThat(schedule.getUpdatedAt()).isAfter(before);
    }

    @Test
    void updateTimezoneShouldChangeTimezoneNextRunAndUpdatedAt() throws InterruptedException {
        Schedule schedule = activeSchedule();
        Instant before = schedule.getUpdatedAt();

        Thread.sleep(2);
        schedule.updateTimezone("UTC", Instant.parse("2024-01-01T04:00:00Z"));

        assertThat(schedule.getTimezone()).isEqualTo("UTC");
        assertThat(schedule.getNextRunAt()).isEqualTo(Instant.parse("2024-01-01T04:00:00Z"));
        assertThat(schedule.getUpdatedAt()).isAfter(before);
    }

    @Test
    void pauseShouldChangeStateToPausedAndRejectRepeatedPause() {
        Schedule schedule = activeSchedule();
        Instant before = schedule.getUpdatedAt();

        schedule.pause();

        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.PAUSED);
        assertThat(schedule.getUpdatedAt()).isAfter(before);
        assertThatThrownBy(schedule::pause)
                .isInstanceOf(InvalidScheduleStateException.class)
                .hasMessage("Schedule is not ACTIVE");
    }

    @Test
    void resumeShouldSwitchPausedScheduleBackToActive() {
        Schedule schedule = pausedSchedule();
        Instant before = schedule.getUpdatedAt();

        schedule.resume(Instant.parse("2024-01-01T05:00:00Z"));

        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.ACTIVE);
        assertThat(schedule.getNextRunAt()).isEqualTo(Instant.parse("2024-01-01T05:00:00Z"));
        assertThat(schedule.getUpdatedAt()).isAfter(before);
    }

    @Test
    void resumeShouldRejectActiveScheduleAndNullNextRun() {
        Schedule active = activeSchedule();
        assertThatThrownBy(() -> active.resume(Instant.now()))
                .isInstanceOf(InvalidScheduleStateException.class)
                .hasMessage("Only a paused schedule can be resumed");

        Schedule paused = pausedSchedule();
        assertThatThrownBy(() -> paused.resume(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("newNextRunAt must not be null");
    }

    @Test
    void advanceNextRunShouldUpdateNextRunAtAndTouchUpdatedAt() {
        Schedule schedule = activeSchedule();
        Instant before = schedule.getUpdatedAt();

        schedule.advanceNextRun(Instant.parse("2024-01-01T06:00:00Z"));

        assertThat(schedule.getNextRunAt()).isEqualTo(Instant.parse("2024-01-01T06:00:00Z"));
        assertThat(schedule.getUpdatedAt()).isAfter(before);
    }

    @Test
    void updateCronAndTimezoneShouldRejectPausedSchedule() {
        Schedule schedule = pausedSchedule();

        assertThatThrownBy(() -> schedule.updateCron("*/10 * * * *", Instant.now()))
                .isInstanceOf(InvalidScheduleStateException.class)
                .hasMessage("Schedule is not ACTIVE");
        assertThatThrownBy(() -> schedule.updateTimezone("UTC", Instant.now()))
                .isInstanceOf(InvalidScheduleStateException.class)
                .hasMessage("Schedule is not ACTIVE");
    }

    private static Schedule activeSchedule() {
        return Schedule.reconstitute(1L, 10L, "*/5 * * * *", "Europe/Moscow", null,
                Instant.parse("2024-01-01T02:00:00Z"), ScheduleStatus.ACTIVE,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"), null);
    }

    private static Schedule pausedSchedule() {
        return Schedule.reconstitute(1L, 10L, "*/5 * * * *", "Europe/Moscow", null,
                Instant.parse("2024-01-01T02:00:00Z"), ScheduleStatus.PAUSED,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"), null);
    }
}
