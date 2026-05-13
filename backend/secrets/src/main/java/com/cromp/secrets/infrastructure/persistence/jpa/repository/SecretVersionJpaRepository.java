package com.cromp.secrets.infrastructure.persistence.jpa.repository;

import com.cromp.secrets.infrastructure.persistence.jpa.entity.SecretVersionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface SecretVersionJpaRepository extends JpaRepository<SecretVersionJpaEntity, Long> {
    Optional<SecretVersionJpaEntity> findBySecretIdAndVersion(Long secretId, int version);
    
    @Query("SELECT v FROM SecretVersionJpaEntity v WHERE v.secretId = ?1 AND v.active = true")
    Optional<SecretVersionJpaEntity> findActiveVersion(Long secretId);
    
    List<SecretVersionJpaEntity> findBySecretIdOrderByVersionDesc(Long secretId);
    
    @Query("SELECT MAX(v.version) FROM SecretVersionJpaEntity v WHERE v.secretId = ?1")
    Optional<Integer> getMaxVersion(Long secretId);
}