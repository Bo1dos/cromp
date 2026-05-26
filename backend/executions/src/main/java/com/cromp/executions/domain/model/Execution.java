package com.cromp.executions.domain.model;

import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.enums.ExecutionStatus;
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
        e.execUuid = UUID.randomUUID();
        e.organizationId = organizationId;
        e.jobId = jobId;
        e.jobVersionId = jobVersionId;
        e.priority = priority;
        e.source = source;
        e.triggeredAt = Instant.now();
        e.scheduledAt = scheduledAt;
        e.finalStatus = ExecutionStatus.CREATED;
        e.totalAttempts = 0;
        e.correlationId = correlationId;
        e.executionPolicySnapshot = executionPolicySnapshot;
        e.createdAt = Instant.now();
        e.updatedAt = Instant.now();
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
        e.id = id;
        e.execUuid = execUuid;
        e.organizationId = organizationId;
        e.jobId = jobId;
        e.jobVersionId = jobVersionId;
        e.priority = priority;
        e.source = source;
        e.triggeredAt = triggeredAt;
        e.scheduledAt = scheduledAt;
        e.finalStatus = finalStatus;
        e.totalAttempts = totalAttempts;
        e.startedAt = startedAt;
        e.finishedAt = finishedAt;
        e.correlationId = correlationId;
        e.executionPolicySnapshot = executionPolicySnapshot;
        e.createdAt = createdAt;
        e.updatedAt = updatedAt;
        return e;
    }

    // Доменные методы — только разрешённые переходы, сама логика в StateMachine
    public void markInProgress() {
        this.finalStatus = ExecutionStatus.IN_PROGRESS;
        if (this.startedAt == null) this.startedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void complete(ExecutionStatus status) {
        this.finalStatus = status;
        this.finishedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.finalStatus = ExecutionStatus.CANCELLED;
        this.finishedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}