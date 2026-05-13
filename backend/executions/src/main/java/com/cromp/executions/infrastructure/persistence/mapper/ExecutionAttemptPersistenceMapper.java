package com.cromp.executions.infrastructure.persistence.mapper;

import com.cromp.executions.domain.model.ExecutionAttempt;
import com.cromp.executions.infrastructure.persistence.jpa.entity.ExecutionAttemptJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ExecutionAttemptPersistenceMapper {
    public ExecutionAttemptJpaEntity toJpa(ExecutionAttempt attempt) {
        return ExecutionAttemptJpaEntity.builder()
                .id(attempt.getId())
                .attemptUuid(attempt.getAttemptUuid())
                .executionId(attempt.getExecutionId())
                .organizationId(attempt.getOrganizationId())
                .attemptNumber(attempt.getAttemptNumber())
                .status(attempt.getStatus())
                .statusReason(attempt.getStatusReason())
                .errorClass(attempt.getErrorClass())
                .scheduledAt(attempt.getScheduledAt())
                .claimedAt(attempt.getClaimedAt())
                .startedAt(attempt.getStartedAt())
                .finishedAt(attempt.getFinishedAt())
                .durationMs(attempt.getDurationMs())
                .executorMetadata(attempt.getExecutorMetadata())
                .idempotencyKey(attempt.getIdempotencyKey())
                .outputSummary(attempt.getOutputSummary())
                .traceId(attempt.getTraceId())
                .createdAt(attempt.getCreatedAt())
                .updatedAt(attempt.getUpdatedAt())
                .build();
    }

    public ExecutionAttempt toDomain(ExecutionAttemptJpaEntity entity) {
        return ExecutionAttempt.reconstitute(
                entity.getId(),
                entity.getAttemptUuid(),
                entity.getExecutionId(),
                entity.getOrganizationId(),
                entity.getAttemptNumber(),
                entity.getStatus(),
                entity.getStatusReason(),
                entity.getErrorClass(),
                entity.getScheduledAt(),
                entity.getClaimedAt(),
                entity.getStartedAt(),
                entity.getFinishedAt(),
                entity.getDurationMs(),
                entity.getExecutorMetadata(),
                entity.getIdempotencyKey(),
                entity.getOutputSummary(),
                entity.getTraceId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
