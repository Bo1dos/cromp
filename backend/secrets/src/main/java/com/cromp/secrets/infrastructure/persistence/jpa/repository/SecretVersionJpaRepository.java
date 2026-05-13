package com.cromp.secrets.infrastructure.persistence.jpa.repository;

import com.cromp.secrets.infrastructure.persistence.jpa.entity.SecretVersionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SecretVersionJpaRepository extends JpaRepository<SecretVersionJpaEntity, Long> {
    Optional<SecretVersionJpaEntity> findFirstBySecretIdAndActiveTrueOrderByVersionDesc(Long secretId);
    Optional<SecretVersionJpaEntity> findBySecretIdAndVersion(Long secretId, int version);
    List<SecretVersionJpaEntity> findBySecretIdOrderByVersionDesc(Long secretId);
    List<SecretVersionJpaEntity> findBySecretIdAndActiveTrue(Long secretId);
}
