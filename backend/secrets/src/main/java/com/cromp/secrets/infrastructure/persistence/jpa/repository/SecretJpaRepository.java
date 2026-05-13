package com.cromp.secrets.infrastructure.persistence.jpa.repository;

import com.cromp.secrets.infrastructure.persistence.jpa.entity.SecretJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SecretJpaRepository extends JpaRepository<SecretJpaEntity, Long> {
    Optional<SecretJpaEntity> findByIdAndOrganizationIdAndDeletedAtIsNull(Long id, Long organizationId);
    List<SecretJpaEntity> findByOrganizationIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long organizationId);
    boolean existsByNameAndOrganizationIdAndDeletedAtIsNull(String name, Long organizationId);
}
