package com.cromp.iam.infrastructure.persistence.mapper;

import com.cromp.iam.domain.model.AuditLogEntry;
import com.cromp.iam.infrastructure.persistence.jpa.entity.AuditLogJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AuditLogPersistenceMapper {

    public AuditLogJpaEntity toJpa(AuditLogEntry entry) {
        return AuditLogJpaEntity.builder()
                .id(entry.getId())
                .organizationId(entry.getOrganizationId())
                .recordedAt(entry.getRecordedAt())
                .actorId(entry.getActorId())
                .actorSnapshot(entry.getActorSnapshot())
                .action(entry.getAction())
                .resourceType(entry.getResourceType())
                .resourceId(entry.getResourceId())
                .changesDiff(entry.getChangesDiff())
                .build();
    }

    public AuditLogEntry toDomain(AuditLogJpaEntity entity) {
        return AuditLogEntry.reconstitute(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getRecordedAt(),
                entity.getActorId(),
                entity.getActorSnapshot(),
                entity.getAction(),
                entity.getResourceType(),
                entity.getResourceId(),
                entity.getChangesDiff()
        );
    }
}