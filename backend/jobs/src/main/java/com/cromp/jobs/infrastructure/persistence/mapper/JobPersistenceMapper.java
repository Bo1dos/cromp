package com.cromp.jobs.infrastructure.persistence.mapper;

import com.cromp.jobs.domain.model.Job;
import com.cromp.jobs.infrastructure.persistence.jpa.entity.JobJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class JobPersistenceMapper {

    public JobJpaEntity toJpa(Job job) {
        return JobJpaEntity.builder()
                .id(job.getId())
                .jobUuid(job.getJobUuid())
                .organizationId(job.getOrganizationId())
                .name(job.getName())
                .description(job.getDescription())
                .status(job.getStatus())
                .queueName(job.getQueueName())
                .priority(job.getPriority())
                .createdBy(job.getCreatedBy())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .deletedAt(job.getDeletedAt())
                .build();
    }

    public Job toDomain(JobJpaEntity entity) {
        return Job.reconstitute(
                entity.getId(),
                entity.getJobUuid(),
                entity.getOrganizationId(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getQueueName(),
                entity.getPriority(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}