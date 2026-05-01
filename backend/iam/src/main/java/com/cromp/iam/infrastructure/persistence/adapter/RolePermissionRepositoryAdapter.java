package com.cromp.iam.infrastructure.persistence.adapter;

import com.cromp.iam.domain.model.RolePermission;
import com.cromp.iam.domain.repository.RolePermissionRepositoryPort;
import com.cromp.iam.infrastructure.persistence.jpa.repository.RolePermissionJpaRepository;
import com.cromp.iam.infrastructure.persistence.mapper.RolePermissionPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class RolePermissionRepositoryAdapter implements RolePermissionRepositoryPort {

    private final RolePermissionJpaRepository repository;
    private final RolePermissionPersistenceMapper mapper;

    @Override
    public RolePermission save(RolePermission rolePermission) {
        return mapper.toDomain(repository.save(mapper.toJpa(rolePermission)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RolePermission> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolePermission> findByRoleId(Long roleId) {
        return repository.findByRoleId(roleId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteByRoleId(Long roleId) {
        repository.deleteByRoleId(roleId);
    }
}