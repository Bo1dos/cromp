package com.cromp.executions.domain.model;

import com.cromp.executions.domain.model.enums.AttemptStatus;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ExecutionAttempt {

    private Long id;
    private UUID attemptUuid;
    private Long executionId;
    private Long organizationId;
    private int attemptNumber;
    private AttemptStatus status;
    private String statusReason;
    private String errorClass;
    private Instant scheduledAt;
    private Instant claimedAt;
    private Instant startedAt;
    private Instant finishedAt;
    private Integer durationMs;
    private String executorMetadata; // JSONB
    private UUID idempotencyKey;
    private String outputSummary;    // JSONB
    private UUID traceId;
    private Instant createdAt;
    private Instant updatedAt;

    private ExecutionAttempt() {}

    public static ExecutionAttempt create(
            Long executionId,
            Long organizationId,
            int attemptNumber,
            Instant scheduledAt,
            UUID idempotencyKey
    ) {
        ExecutionAttempt a = new ExecutionAttempt();
        a.attemptUuid = UUID.randomUUID();
        a.executionId = executionId;
        a.organizationId = organizationId;
        a.attemptNumber = attemptNumber;
        a.status = AttemptStatus.PENDING;
        a.scheduledAt = scheduledAt;
        a.idempotencyKey = idempotencyKey != null ? idempotencyKey : UUID.randomUUID();
        a.createdAt = Instant.now();
        a.updatedAt = Instant.now();
        return a;
    }

    public static ExecutionAttempt reconstitute(
            Long id, UUID attemptUuid, Long executionId, Long organizationId,
            int attemptNumber, AttemptStatus status, String statusReason, String errorClass,
            Instant scheduledAt, Instant claimedAt, Instant startedAt, Instant finishedAt,
            Integer durationMs, String executorMetadata, UUID idempotencyKey,
            String outputSummary, UUID traceId, Instant createdAt, Instant updatedAt
    ) {
        ExecutionAttempt a = new ExecutionAttempt();
        a.id = id;
        a.attemptUuid = attemptUuid;
        a.executionId = executionId;
        a.organizationId = organizationId;
        a.attemptNumber = attemptNumber;
        a.status = status;
        a.statusReason = statusReason;
        a.errorClass = errorClass;
        a.scheduledAt = scheduledAt;
        a.claimedAt = claimedAt;
        a.startedAt = startedAt;
        a.finishedAt = finishedAt;
        a.durationMs = durationMs;
        a.executorMetadata = executorMetadata;
        a.idempotencyKey = idempotencyKey;
        a.outputSummary = outputSummary;
        a.traceId = traceId;
        a.createdAt = createdAt;
        a.updatedAt = updatedAt;
        return a;
    }

    public void dispatch() {
        this.status = AttemptStatus.DISPATCHED;
        this.claimedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void start(UUID traceId) {
        this.status = AttemptStatus.RUNNING;
        this.startedAt = Instant.now();
        this.traceId = traceId;
        this.updatedAt = Instant.now();
    }

    public void complete(AttemptStatus terminalStatus, String outputSummary,
                         String errorClass, String statusReason) {
        this.status = terminalStatus;
        this.outputSummary = outputSummary;
        this.errorClass = errorClass;
        this.statusReason = statusReason;
        this.finishedAt = Instant.now();
        if (this.startedAt != null) {
            this.durationMs = (int) (finishedAt.toEpochMilli() - startedAt.toEpochMilli());
        }
        this.updatedAt = Instant.now();
    }

    public void cancel(String reason) {
        this.status = AttemptStatus.CANCELLED;
        this.statusReason = reason;
        this.finishedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void markTimeout() {
        this.status = AttemptStatus.TIMEOUT;
        this.statusReason = "Timed out by janitor";
        this.finishedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}