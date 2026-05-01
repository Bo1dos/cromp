package com.cromp.iam.infrastructure.persistence.jpa.repository;

import com.cromp.iam.domain.model.enums.UserRole;
import com.cromp.iam.infrastructure.persistence.jpa.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, Long> {
    Optional<RoleJpaEntity> findByName(UserRole name);
}