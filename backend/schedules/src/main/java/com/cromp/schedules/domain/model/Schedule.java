package com.cromp.schedules.domain.model;

import com.cromp.schedules.domain.model.enums.ScheduleStatus;
import com.cromp.schedules.domain.model.exceptions.InvalidScheduleStateException;
import com.cromp.schedules.domain.model.support.AbstractAuditableDomainEntity;
import com.cromp.schedules.domain.model.support.DomainChecks;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Schedule extends AbstractAuditableDomainEntity {

    private static final int CRON_EXPRESSION_MAX_LENGTH = 128;
    private static final int TIMEZONE_MAX_LENGTH = 64;

    @EqualsAndHashCode.Include
    private Long jobId;               

    private String cronExpression;
    private String timezone;
    private String rules;             // JSONB, пока как строка
    private Instant nextRunAt;
    private ScheduleStatus status;

    // Приватный конструктор для полноценной инициализации
    private Schedule(Long id, Instant createdAt, Instant updatedAt, Instant deletedAt,
                     Long jobId, String cronExpression, String timezone, String rules,
                     Instant nextRunAt, ScheduleStatus status) {
        super(id, createdAt, updatedAt, deletedAt);
        this.jobId = DomainChecks.requireNonNullValue(jobId, "jobId");
        this.cronExpression = DomainChecks.requireMaxLength(
                DomainChecks.requireText(cronExpression, "cronExpression"),
                CRON_EXPRESSION_MAX_LENGTH,
                "cronExpression"
        );
        this.timezone = DomainChecks.requireMaxLength(
                DomainChecks.requireText(timezone, "timezone"),
                TIMEZONE_MAX_LENGTH,
                "timezone"
        );
        this.rules = rules; // может быть null
        this.nextRunAt = nextRunAt; // может быть null для paused?
        this.status = DomainChecks.requireNonNullValue(status, "status");
    }

    // Фабричный метод создания нового расписания
    public static Schedule create(Long jobId, String cronExpression, String timezone,
                                  String rules, Instant nextRunAt) {
        Instant now = Instant.now();
        return new Schedule(null, now, now, null, jobId, cronExpression, timezone,
                            rules, nextRunAt, ScheduleStatus.ACTIVE);
    }

    // Восстановление из БД
    public static Schedule reconstitute(Long id, Long jobId, String cronExpression,
                                        String timezone, String rules,
                                        Instant nextRunAt, ScheduleStatus status,
                                        Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new Schedule(id, createdAt, updatedAt, deletedAt, jobId, cronExpression,
                            timezone, rules, nextRunAt, status);
    }

    // ----------------- Поведенческие методы -----------------

    public void updateCron(String newCronExpression, Instant newNextRunAt) {
        ensureActive();
        this.cronExpression = DomainChecks.requireMaxLength(
                DomainChecks.requireText(newCronExpression, "cronExpression"),
                CRON_EXPRESSION_MAX_LENGTH,
                "cronExpression"
        );
        this.nextRunAt = newNextRunAt;
        touch();
    }

    public void updateTimezone(String newTimezone, Instant newNextRunAt) {
        ensureActive();
        this.timezone = DomainChecks.requireMaxLength(
                DomainChecks.requireText(newTimezone, "timezone"),
                TIMEZONE_MAX_LENGTH,
                "timezone"
        );
        this.nextRunAt = newNextRunAt;
        touch();
    }

    public void pause() {
        ensureActive();
        this.status = ScheduleStatus.PAUSED;
        // nextRunAt не обнуляем, просто перестаём учитывать в выборках
        touch();
    }

    public void resume(Instant newNextRunAt) {
        if (this.status != ScheduleStatus.PAUSED) {
            throw new InvalidScheduleStateException("Only a paused schedule can be resumed");
        }
        DomainChecks.requireNonNullValue(newNextRunAt, "newNextRunAt");
        this.status = ScheduleStatus.ACTIVE;
        this.nextRunAt = newNextRunAt;
        touch();
    }

    // Обновление nextRunAt после выполнения задачи (вызовется orchestrator’ом)
    public void advanceNextRun(Instant newNextRunAt) {
        DomainChecks.requireNonNullValue(newNextRunAt, "newNextRunAt");
        this.nextRunAt = newNextRunAt;
        touch();
    }

    private void ensureActive() {
        if (this.status != ScheduleStatus.ACTIVE) {
            throw new InvalidScheduleStateException("Schedule is not ACTIVE");
        }
    }
}
