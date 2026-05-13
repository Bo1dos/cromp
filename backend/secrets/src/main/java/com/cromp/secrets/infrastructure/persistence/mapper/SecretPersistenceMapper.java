package com.cromp.secrets.infrastructure.persistence.mapper;

import com.cromp.secrets.domain.model.Secret;
import com.cromp.secrets.infrastructure.persistence.jpa.entity.SecretJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SecretPersistenceMapper {
    public SecretJpaEntity toJpa(Secret secret) {
        return SecretJpaEntity.builder()
                .id(secret.getId())
                .secretUuid(secret.getSecretUuid())
                .organizationId(secret.getOrganizationId())
                .name(secret.getName())
                .scope(secret.getScope())
                .description(secret.getDescription())
                .createdAt(secret.getCreatedAt())
                .updatedAt(secret.getUpdatedAt())
                .deletedAt(secret.getDeletedAt())
                .build();
    }

    public Secret toDomain(SecretJpaEntity entity) {
        return Secret.reconstitute(
                entity.getId(),
                entity.getSecretUuid(),
                entity.getOrganizationId(),
                entity.getName(),
                entity.getScope(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}
