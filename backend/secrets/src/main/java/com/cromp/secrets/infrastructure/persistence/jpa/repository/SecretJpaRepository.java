package com.cromp.secrets.infrastructure.persistence.jpa.repository;

import com.cromp.secrets.infrastructure.persistence.jpa.entity.SecretJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SecretJpaRepository extends JpaRepository<SecretJpaEntity, Long> {
    Optional<SecretJpaEntity> findBySecretUuid(UUID secretUuid);
    Optional<SecretJpaEntity> findByNameAndOrganizationIdAndDeletedAtIsNull(String name, Long organizationId);
    List<SecretJpaEntity> findByOrganizationIdAndDeletedAtIsNull(Long organizationId);
}