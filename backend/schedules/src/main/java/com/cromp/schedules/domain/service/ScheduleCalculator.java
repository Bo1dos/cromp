package com.cromp.schedules.domain.service;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.cromp.schedules.domain.model.exceptions.InvalidCronExpressionException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class ScheduleCalculator {

    private final CronParser cronParser;
    private final Clock clock;

    public ScheduleCalculator() {
        this(Clock.systemDefaultZone());
    }

    public ScheduleCalculator(Clock clock) {
        // По умолчанию используем стандартный UNIX cron (5 полей)
        this.cronParser = new CronParser(
                CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        this.clock = clock;
    }

    /**
     * Проверяет корректность cron-выражения и возвращает его представление.
     * @throws InvalidCronExpressionException если выражение некорректно.
     */
    public Cron validate(String expression) {
        try {
            return cronParser.parse(expression);
        } catch (IllegalArgumentException e) {
            throw new InvalidCronExpressionException(expression, e.getMessage());
        }
    }

    /**
     * Вычисляет следующий момент запуска от текущего времени (now).
     * @param cronExpression проверенное cron-выражение
     * @param timezone      временная зона (например, "Europe/Moscow")
     * @return Instant следующего запуска
     */
    public Instant calculateNextRun(Cron cronExpression, String timezone) {
        ZoneId zoneId = ZoneId.of(timezone);
        ZonedDateTime now = ZonedDateTime.now(clock.withZone(zoneId));
        ExecutionTime executionTime = ExecutionTime.forCron(cronExpression);
        return executionTime.nextExecution(now)
                .orElseThrow(() -> new InvalidCronExpressionException(
                        cronExpression.asString(), "No next execution time found"))
                .toInstant();
    }
}
