package com.cromp.iam.infrastructure.persistence.mapper;

import com.cromp.iam.domain.model.Organization;
import com.cromp.iam.infrastructure.persistence.jpa.entity.OrganizationJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class OrganizationPersistenceMapper {

    public OrganizationJpaEntity toJpa(Organization organization) {
        return OrganizationJpaEntity.builder()
                .id(organization.getId())
                .orgUuid(organization.getOrgUuid())
                .name(organization.getName())
                .settings(organization.getSettings())
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .deletedAt(organization.getDeletedAt())
                .build();
    }

    public Organization toDomain(OrganizationJpaEntity entity) {
        return Organization.reconstitute(
                entity.getId(),
                entity.getOrgUuid(),
                entity.getName(),
                entity.getSettings(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}