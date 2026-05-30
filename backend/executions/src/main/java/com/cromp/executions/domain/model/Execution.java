package com.cromp.executions.domain.model;

import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.enums.ExecutionStatus;
import com.cromp.executions.domain.model.support.DomainChecks;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class Execution {

    private Long id;
    private UUID execUuid;
    private Long organizationId;
    private Long jobId;
    private Long jobVersionId;
    private int priority;
    private ExecutionSource source;
    private Instant triggeredAt;
    private Instant scheduledAt;
    private ExecutionStatus finalStatus;
    private int totalAttempts;
    private Instant startedAt;
    private Instant finishedAt;
    private UUID correlationId;
    private String executionPolicySnapshot; // JSONB как строка
    private Instant createdAt;
    private Instant updatedAt;

    private Execution() {}

    // Фабрика для создания нового Execution
    public static Execution create(
            Long organizationId,
            Long jobId,
            Long jobVersionId,
            int priority,
            ExecutionSource source,
            Instant scheduledAt,
            UUID correlationId,
            String executionPolicySnapshot
    ) {
        Execution e = new Execution();
        e.organizationId = DomainChecks.requireNonNullValue(organizationId, "organizationId");
        e.jobId = DomainChecks.requireNonNullValue(jobId, "jobId");
        e.jobVersionId = DomainChecks.requireNonNullValue(jobVersionId, "jobVersionId");
        e.execUuid = UUID.randomUUID();
        e.priority = priority;
        e.source = DomainChecks.requireNonNullValue(source, "source");
        Instant now = Instant.now();
        e.triggeredAt = now;
        e.scheduledAt = scheduledAt;
        e.finalStatus = ExecutionStatus.CREATED;
        e.totalAttempts = 0;
        e.correlationId = correlationId;
        e.executionPolicySnapshot = executionPolicySnapshot;
        e.createdAt = now;
        e.updatedAt = now;
        return e;
    }

    // Фабрика для восстановления из БД
    public static Execution reconstitute(
            Long id, UUID execUuid, Long organizationId, Long jobId, Long jobVersionId,
            int priority, ExecutionSource source, Instant triggeredAt, Instant scheduledAt,
            ExecutionStatus finalStatus, int totalAttempts, Instant startedAt,
            Instant finishedAt, UUID correlationId, String executionPolicySnapshot,
            Instant createdAt, Instant updatedAt
    ) {
        Execution e = new Execution();
        e.id = DomainChecks.requireNonNullValue(id, "id");
        e.execUuid = DomainChecks.requireNonNullValue(execUuid, "execUuid");
        e.organizationId = DomainChecks.requireNonNullValue(organizationId, "organizationId");
        e.jobId = DomainChecks.requireNonNullValue(jobId, "jobId");
        e.jobVersionId = jobVersionId;
        e.priority = priority;
        e.source = DomainChecks.requireNonNullValue(source, "source");
        e.triggeredAt = triggeredAt;
        e.scheduledAt = scheduledAt;
        e.finalStatus = DomainChecks.requireNonNullValue(finalStatus, "finalStatus");
        e.totalAttempts = totalAttempts;
        e.startedAt = startedAt;
        e.finishedAt = finishedAt;
        e.correlationId = correlationId;
        e.executionPolicySnapshot = executionPolicySnapshot;
        e.createdAt = DomainChecks.requireNonNullValue(createdAt, "createdAt");
        e.updatedAt = DomainChecks.requireNonNullValue(updatedAt, "updatedAt");
        return e;
    }

    // Доменные методы — только разрешённые переходы, сама логика в StateMachine
    public void markInProgress() {
        this.finalStatus = ExecutionStatus.IN_PROGRESS;
        Instant now = Instant.now();
        if (this.startedAt == null) this.startedAt = now;
        this.updatedAt = now;
    }

    public void complete(ExecutionStatus status) {
        this.finalStatus = status;
        Instant now = Instant.now();
        this.finishedAt = now;
        this.updatedAt = now;
    }

    public void cancel() {
        this.finalStatus = ExecutionStatus.CANCELLED;
        Instant now = Instant.now();
        this.finishedAt = now;
        this.updatedAt = now;
    }
}
