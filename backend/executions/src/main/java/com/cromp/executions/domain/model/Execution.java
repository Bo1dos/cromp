package com.cromp.executions.domain.model;

import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.enums.ExecutionStatus;
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
public class Execution {
    @EqualsAndHashCode.Include
    private UUID execUuid;
    private Long id;
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
    private Map<String, Object> executionPolicySnapshot;
    private Instant createdAt;
    private Instant updatedAt;

    private Execution(Long id, UUID execUuid, Long organizationId, Long jobId, Long jobVersionId, int priority,
                      ExecutionSource source, Instant triggeredAt, Instant scheduledAt, ExecutionStatus finalStatus,
                      int totalAttempts, Instant startedAt, Instant finishedAt, UUID correlationId,
                      Map<String, Object> executionPolicySnapshot, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.execUuid = execUuid == null ? UUID.randomUUID() : execUuid;
        this.organizationId = requireNonNullValue(organizationId, "organizationId");
        this.jobId = requireNonNullValue(jobId, "jobId");
        this.jobVersionId = jobVersionId;
        this.priority = priority;
        this.source = requireNonNullValue(source, "source");
        this.triggeredAt = requireNonNullValue(triggeredAt, "triggeredAt");
        this.scheduledAt = scheduledAt;
        this.finalStatus = requireNonNullValue(finalStatus, "finalStatus");
        this.totalAttempts = totalAttempts;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.correlationId = correlationId;
        this.executionPolicySnapshot = safeMap(executionPolicySnapshot);
        this.createdAt = requireNonNullValue(createdAt, "createdAt");
        this.updatedAt = requireNonNullValue(updatedAt, "updatedAt");
    }

    public static Execution create(UUID execUuid, Long organizationId, Long jobId, Long jobVersionId,
                                   int priority, ExecutionSource source, Instant scheduledAt,
                                   UUID correlationId, Map<String, Object> policySnapshot) {
        Instant now = Instant.now();
        return new Execution(null, execUuid, organizationId, jobId, jobVersionId, priority, source, now,
                scheduledAt, ExecutionStatus.CREATED, 0, null, null, correlationId, policySnapshot, now, now);
    }

    public static Execution reconstitute(Long id, UUID execUuid, Long organizationId, Long jobId, Long jobVersionId,
                                         int priority, ExecutionSource source, Instant triggeredAt, Instant scheduledAt,
                                         ExecutionStatus finalStatus, int totalAttempts, Instant startedAt,
                                         Instant finishedAt, UUID correlationId, Map<String, Object> policySnapshot,
                                         Instant createdAt, Instant updatedAt) {
        return new Execution(id, execUuid, organizationId, jobId, jobVersionId, priority, source, triggeredAt,
                scheduledAt, finalStatus, totalAttempts, startedAt, finishedAt, correlationId, policySnapshot,
                createdAt, updatedAt);
    }

    public void cancel() {
        if (finalStatus == ExecutionStatus.SUCCEEDED || finalStatus == ExecutionStatus.FAILED) {
            throw new IllegalStateException("Finished execution cannot be cancelled");
        }
        finalStatus = ExecutionStatus.CANCELLED;
        finishedAt = Instant.now();
        updatedAt = finishedAt;
    }
}
