package com.cromp.executions.infrastructure.persistence.mapper;

import com.cromp.executions.domain.model.ExecutionAttempt;
import com.cromp.executions.infrastructure.persistence.jpa.entity.ExecutionAttemptJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ExecutionAttemptPersistenceMapper {

    public ExecutionAttemptJpaEntity toJpa(ExecutionAttempt a) {
        return ExecutionAttemptJpaEntity.builder()
                .id(a.getId())
                .attemptUuid(a.getAttemptUuid())
                .executionId(a.getExecutionId())
                .organizationId(a.getOrganizationId())
                .attemptNumber(a.getAttemptNumber())
                .status(a.getStatus())
                .statusReason(a.getStatusReason())
                .errorClass(a.getErrorClass())
                .scheduledAt(a.getScheduledAt())
                .claimedAt(a.getClaimedAt())
                .startedAt(a.getStartedAt())
                .finishedAt(a.getFinishedAt())
                .durationMs(a.getDurationMs())
                .executorMetadata(a.getExecutorMetadata())
                .idempotencyKey(a.getIdempotencyKey())
                .outputSummary(a.getOutputSummary())
                .traceId(a.getTraceId())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
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