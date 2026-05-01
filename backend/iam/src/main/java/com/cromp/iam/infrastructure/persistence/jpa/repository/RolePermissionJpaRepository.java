package com.cromp.iam.infrastructure.persistence.jpa.repository;

import com.cromp.iam.infrastructure.persistence.jpa.entity.RolePermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionJpaRepository extends JpaRepository<RolePermissionJpaEntity, Long> {
    List<RolePermissionJpaEntity> findByRoleId(Long roleId);
    void deleteByRoleId(Long roleId);
}