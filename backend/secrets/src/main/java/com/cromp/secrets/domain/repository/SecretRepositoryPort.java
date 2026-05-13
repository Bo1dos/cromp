package com.cromp.secrets.domain.repository;

import com.cromp.secrets.domain.model.Secret;

import java.util.List;
import java.util.Optional;

public interface SecretRepositoryPort {
    Secret save(Secret secret);
    Optional<Secret> findByIdAndOrganizationId(Long id, Long organizationId);
    List<Secret> findByOrganizationId(Long organizationId);
    boolean existsByNameAndOrganizationId(String name, Long organizationId);
}
