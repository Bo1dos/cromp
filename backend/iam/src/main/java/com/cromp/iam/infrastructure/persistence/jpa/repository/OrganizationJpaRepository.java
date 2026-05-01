package com.cromp.iam.infrastructure.persistence.jpa.repository;

import com.cromp.iam.infrastructure.persistence.jpa.entity.OrganizationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationJpaRepository extends JpaRepository<OrganizationJpaEntity, Long> {
    Optional<OrganizationJpaEntity> findByOrgUuid(UUID orgUuid);
    Optional<OrganizationJpaEntity> findByName(String name);
    boolean existsByName(String name);
}