package com.cromp.executions.infrastructure.persistence.mapper;

import com.cromp.executions.domain.model.Execution;
import com.cromp.executions.infrastructure.persistence.jpa.entity.ExecutionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ExecutionPersistenceMapper {
    public ExecutionJpaEntity toJpa(Execution execution) {
        return ExecutionJpaEntity.builder()
                .id(execution.getId())
                .execUuid(execution.getExecUuid())
                .organizationId(execution.getOrganizationId())
                .jobId(execution.getJobId())
                .jobVersionId(execution.getJobVersionId())
                .priority(execution.getPriority())
                .source(execution.getSource())
                .triggeredAt(execution.getTriggeredAt())
                .scheduledAt(execution.getScheduledAt())
                .finalStatus(execution.getFinalStatus())
                .totalAttempts(execution.getTotalAttempts())
                .startedAt(execution.getStartedAt())
                .finishedAt(execution.getFinishedAt())
                .correlationId(execution.getCorrelationId())
                .executionPolicySnapshot(execution.getExecutionPolicySnapshot())
                .createdAt(execution.getCreatedAt())
                .updatedAt(execution.getUpdatedAt())
                .build();
    }

    public Execution toDomain(ExecutionJpaEntity entity) {
        return Execution.reconstitute(
                entity.getId(),
                entity.getExecUuid(),
                entity.getOrganizationId(),
                entity.getJobId(),
                entity.getJobVersionId(),
                entity.getPriority(),
                entity.getSource(),
                entity.getTriggeredAt(),
                entity.getScheduledAt(),
                entity.getFinalStatus(),
                entity.getTotalAttempts(),
                entity.getStartedAt(),
                entity.getFinishedAt(),
                entity.getCorrelationId(),
                entity.getExecutionPolicySnapshot(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
