package com.cromp.secrets.domain.repository;

import com.cromp.secrets.domain.model.SecretVersion;

import java.util.List;
import java.util.Optional;

public interface SecretVersionRepositoryPort {
    SecretVersion save(SecretVersion version);
    Optional<SecretVersion> findActiveBySecretId(Long secretId);
    Optional<SecretVersion> findBySecretIdAndVersion(Long secretId, int version);
    List<SecretVersion> findBySecretId(Long secretId);
    void deactivateActiveVersions(Long secretId);
}
