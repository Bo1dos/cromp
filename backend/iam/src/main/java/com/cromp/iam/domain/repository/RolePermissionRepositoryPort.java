package com.cromp.iam.domain.repository;

import com.cromp.iam.domain.model.RolePermission;

import java.util.List;
import java.util.Optional;

public interface RolePermissionRepositoryPort {
    RolePermission save(RolePermission rolePermission);
    Optional<RolePermission> findById(Long id);
    List<RolePermission> findByRoleId(Long roleId);
    void deleteById(Long id);
    void deleteByRoleId(Long roleId);
}