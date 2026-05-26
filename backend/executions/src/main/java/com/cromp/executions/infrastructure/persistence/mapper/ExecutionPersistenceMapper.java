package com.cromp.executions.infrastructure.persistence.mapper;

import com.cromp.executions.domain.model.Execution;
import com.cromp.executions.infrastructure.persistence.jpa.entity.ExecutionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ExecutionPersistenceMapper {

    public ExecutionJpaEntity toJpa(Execution e) {
        return ExecutionJpaEntity.builder()
                .id(e.getId())
                .execUuid(e.getExecUuid())
                .organizationId(e.getOrganizationId())
                .jobId(e.getJobId())
                .jobVersionId(e.getJobVersionId())
                .priority(e.getPriority())
                .source(e.getSource())
                .triggeredAt(e.getTriggeredAt())
                .scheduledAt(e.getScheduledAt())
                .finalStatus(e.getFinalStatus())
                .totalAttempts(e.getTotalAttempts())
                .startedAt(e.getStartedAt())
                .finishedAt(e.getFinishedAt())
                .correlationId(e.getCorrelationId())
                .executionPolicySnapshot(e.getExecutionPolicySnapshot())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
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