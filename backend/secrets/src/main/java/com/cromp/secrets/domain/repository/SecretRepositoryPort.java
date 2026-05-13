package com.cromp.secrets.domain.repository;

import com.cromp.secrets.domain.model.Secret;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SecretRepositoryPort {
    Secret save(Secret secret);
    Optional<Secret> findById(Long id);
    Optional<Secret> findBySecretUuid(UUID secretUuid);
    Optional<Secret> findByNameAndOrganizationId(String name, Long organizationId);
    List<Secret> findByOrganizationId(Long organizationId);
    void delete(Secret secret);
}