package com.cromp.secrets.domain.repository;

import com.cromp.secrets.domain.model.SecretVersion;
import java.util.List;
import java.util.Optional;

public interface SecretVersionRepositoryPort {
    SecretVersion save(SecretVersion version);
    Optional<SecretVersion> findById(Long id);
    Optional<SecretVersion> findBySecretIdAndVersion(Long secretId, int version);
    Optional<SecretVersion> findActiveVersion(Long secretId);
    List<SecretVersion> findBySecretIdOrderByVersionDesc(Long secretId);
    Optional<Integer> getMaxVersion(Long secretId);
}