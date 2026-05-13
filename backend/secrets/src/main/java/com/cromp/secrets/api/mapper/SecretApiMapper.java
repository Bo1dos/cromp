package com.cromp.secrets.api.mapper;

import com.cromp.secrets.api.dto.response.SecretResponse;
import com.cromp.secrets.api.dto.response.SecretVersionResponse;
import com.cromp.secrets.domain.model.Secret;
import com.cromp.secrets.domain.model.SecretVersion;
import org.springframework.stereotype.Component;

@Component
public class SecretApiMapper {

    public SecretResponse toSecretResponse(Secret secret, SecretVersion activeVersion) {
        return new SecretResponse(
                secret.getId(),
                secret.getSecretUuid(),
                secret.getOrganizationId(),
                secret.getName(),
                secret.getScope().name(),
                secret.getDescription(),
                activeVersion != null ? activeVersion.getVersion() : 0,
                secret.getCreatedAt(),
                secret.getUpdatedAt()
        );
    }

    public SecretVersionResponse toVersionResponse(SecretVersion version) {
        return new SecretVersionResponse(
                version.getId(),
                version.getVersion(),
                version.isActive(),
                version.getCreatedBy(),
                version.getCreatedAt(),
                version.getDeprecatedAt()
        );
    }
}