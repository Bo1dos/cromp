package com.cromp.iam.domain.repository;

import com.cromp.iam.domain.model.Role;
import com.cromp.iam.domain.model.enums.UserRole;

import java.util.List;
import java.util.Optional;

public interface RoleRepositoryPort {
    Role save(Role role);
    Optional<Role> findById(Long id);
    Optional<Role> findByName(UserRole name);
    List<Role> findAll();
}