package com.cromp.schedules.domain.service;

import com.cronutils.model.Cron;
import com.cromp.schedules.domain.model.exceptions.InvalidCronExpressionException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScheduleCalculatorTest {

    @Test
    void validateShouldThrowForNullExpression() {
        ScheduleCalculator calculator = new ScheduleCalculator();

        assertThatThrownBy(() -> calculator.validate(null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void validateShouldReturnCronForValidExpressionAndThrowForInvalidOne() {
        ScheduleCalculator calculator = new ScheduleCalculator();

        Cron cron = calculator.validate("*/5 * * * *");

        assertThat(cron.asString()).isEqualTo("*/5 * * * *");
        assertThatThrownBy(() -> calculator.validate("not a cron"))
                .isInstanceOf(InvalidCronExpressionException.class)
                .hasMessageContaining("Invalid cron expression");
    }

    @Test
    void calculateNextRunShouldReturnInstantAfterNowForValidCronAndTimezone() {
        Clock clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
        ScheduleCalculator calculator = new ScheduleCalculator(clock);
        Cron cron = calculator.validate("*/5 * * * *");

        Instant nextRun = calculator.calculateNextRun(cron, "Europe/Moscow");

        assertThat(nextRun).isAfter(Instant.parse("2024-01-01T00:00:00Z"));
    }

    @Test
    void calculateNextRunShouldThrowForUnknownTimezone() {
        ScheduleCalculator calculator = new ScheduleCalculator();
        Cron cron = calculator.validate("*/5 * * * *");

        assertThatThrownBy(() -> calculator.calculateNextRun(cron, "No/Such_Zone"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void calculateNextRunShouldThrowWhenNoNextExecutionExists() {
        Clock clock = Clock.fixed(Instant.parse("2024-12-31T23:59:59Z"), ZoneOffset.UTC);
        ScheduleCalculator calculator = new ScheduleCalculator(clock);
        Cron cron = calculator.validate("0 0 30 2 *"); // Feb 30 – never exists

        assertThatThrownBy(() -> calculator.calculateNextRun(cron, "UTC"))
                .isInstanceOf(InvalidCronExpressionException.class)
                .hasMessageContaining("No next execution time found");
    }
}
