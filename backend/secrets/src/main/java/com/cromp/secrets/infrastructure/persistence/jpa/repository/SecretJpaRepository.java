package com.cromp.secrets.infrastructure.persistence.jpa.repository;

import com.cromp.secrets.infrastructure.persistence.jpa.entity.SecretJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SecretJpaRepository extends JpaRepository<SecretJpaEntity, Long> {
    @Query("SELECT s FROM SecretJpaEntity s WHERE s.secretUuid = ?1 AND s.deletedAt IS NULL")
    Optional<SecretJpaEntity> findBySecretUuid(UUID secretUuid);
    Optional<SecretJpaEntity> findByNameAndOrganizationIdAndDeletedAtIsNull(String name, Long organizationId);
    List<SecretJpaEntity> findByOrganizationIdAndDeletedAtIsNull(Long organizationId);
}
