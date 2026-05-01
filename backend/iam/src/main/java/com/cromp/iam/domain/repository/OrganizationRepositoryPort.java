package com.cromp.iam.domain.repository;

import com.cromp.iam.domain.model.Organization;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepositoryPort {
    Organization save(Organization organization);
    Optional<Organization> findById(Long id);
    Optional<Organization> findByOrgUuid(UUID orgUuid);
    Optional<Organization> findByName(String name);
    boolean existsByName(String name);
}