package com.cromp.secrets.infrastructure.persistence.mapper;

import com.cromp.secrets.domain.model.SecretVersion;
import com.cromp.secrets.infrastructure.persistence.jpa.entity.SecretVersionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SecretVersionPersistenceMapper {
    public SecretVersionJpaEntity toJpa(SecretVersion version) {
        return SecretVersionJpaEntity.builder()
                .id(version.getId())
                .secretId(version.getSecretId())
                .version(version.getVersion())
                .valueCipher(version.getValueCipher())
                .keyId(version.getKeyId())
                .active(version.isActive())
                .createdBy(version.getCreatedBy())
                .createdAt(version.getCreatedAt())
                .deprecatedAt(version.getDeprecatedAt())
                .build();
    }

    public SecretVersion toDomain(SecretVersionJpaEntity entity) {
        return SecretVersion.reconstitute(
                entity.getId(),
                entity.getSecretId(),
                entity.getVersion(),
                entity.getValueCipher(),
                entity.getKeyId(),
                entity.isActive(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getDeprecatedAt()
        );
    }
}
