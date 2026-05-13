package com.cromp.executions.domain.model;

import com.cromp.executions.domain.model.enums.AttemptStatus;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static com.cromp.executions.domain.model.support.DomainChecks.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ExecutionAttempt {
    @EqualsAndHashCode.Include
    private UUID attemptUuid;
    private Long id;
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
    private Map<String, Object> executorMetadata;
    private UUID idempotencyKey;
    private Map<String, Object> outputSummary;
    private UUID traceId;
    private Instant createdAt;
    private Instant updatedAt;

    private ExecutionAttempt(Long id, UUID attemptUuid, Long executionId, Long organizationId, int attemptNumber,
                             AttemptStatus status, String statusReason, String errorClass, Instant scheduledAt,
                             Instant claimedAt, Instant startedAt, Instant finishedAt, Integer durationMs,
                             Map<String, Object> executorMetadata, UUID idempotencyKey,
                             Map<String, Object> outputSummary, UUID traceId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.attemptUuid = attemptUuid == null ? UUID.randomUUID() : attemptUuid;
        this.executionId = requireNonNullValue(executionId, "executionId");
        this.organizationId = requireNonNullValue(organizationId, "organizationId");
        if (attemptNumber < 1) throw new IllegalArgumentException("attemptNumber must be positive");
        this.attemptNumber = attemptNumber;
        this.status = requireNonNullValue(status, "status");
        this.statusReason = statusReason;
        this.errorClass = errorClass;
        this.scheduledAt = scheduledAt;
        this.claimedAt = claimedAt;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.durationMs = durationMs;
        this.executorMetadata = safeMap(executorMetadata);
        this.idempotencyKey = idempotencyKey;
        this.outputSummary = safeMap(outputSummary);
        this.traceId = traceId;
        this.createdAt = requireNonNullValue(createdAt, "createdAt");
        this.updatedAt = requireNonNullValue(updatedAt, "updatedAt");
    }

    public static ExecutionAttempt create(Long executionId, Long organizationId, int attemptNumber, Instant scheduledAt) {
        Instant now = Instant.now();
        return new ExecutionAttempt(null, UUID.randomUUID(), executionId, organizationId, attemptNumber,
                AttemptStatus.PENDING, null, null, scheduledAt, null, null, null, null,
                Map.of(), null, Map.of(), null, now, now);
    }

    public static ExecutionAttempt reconstitute(Long id, UUID attemptUuid, Long executionId, Long organizationId,
                                                int attemptNumber, AttemptStatus status, String statusReason,
                                                String errorClass, Instant scheduledAt, Instant claimedAt,
                                                Instant startedAt, Instant finishedAt, Integer durationMs,
                                                Map<String, Object> executorMetadata, UUID idempotencyKey,
                                                Map<String, Object> outputSummary, UUID traceId,
                                                Instant createdAt, Instant updatedAt) {
        return new ExecutionAttempt(id, attemptUuid, executionId, organizationId, attemptNumber, status,
                statusReason, errorClass, scheduledAt, claimedAt, startedAt, finishedAt, durationMs,
                executorMetadata, idempotencyKey, outputSummary, traceId, createdAt, updatedAt);
    }

    public void changeStatus(AttemptStatus status, String reason, String errorClass, Map<String, Object> outputSummary) {
        this.status = requireNonNullValue(status, "status");
        this.statusReason = reason;
        this.errorClass = errorClass;
        this.outputSummary = safeMap(outputSummary);
        Instant now = Instant.now();
        if (status == AttemptStatus.RUNNING && startedAt == null) startedAt = now;
        if (status == AttemptStatus.SUCCEEDED || status == AttemptStatus.FAILED
                || status == AttemptStatus.TIMEOUT || status == AttemptStatus.CANCELLED) {
            finishedAt = now;
            if (startedAt != null) durationMs = Math.toIntExact(Math.max(0, finishedAt.toEpochMilli() - startedAt.toEpochMilli()));
        }
        updatedAt = now;
    }
}
