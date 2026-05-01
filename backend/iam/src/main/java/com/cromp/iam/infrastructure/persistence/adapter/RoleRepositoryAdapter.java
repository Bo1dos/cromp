package com.cromp.iam.infrastructure.persistence.adapter;

import com.cromp.iam.domain.model.Role;
import com.cromp.iam.domain.model.enums.UserRole;
import com.cromp.iam.domain.repository.RoleRepositoryPort;
import com.cromp.iam.infrastructure.persistence.jpa.repository.RoleJpaRepository;
import com.cromp.iam.infrastructure.persistence.mapper.RolePersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class RoleRepositoryAdapter implements RoleRepositoryPort {

    private final RoleJpaRepository repository;
    private final RolePersistenceMapper mapper;

    @Override
    public Role save(Role role) {
        return mapper.toDomain(repository.save(mapper.toJpa(role)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByName(UserRole name) {
        return repository.findByName(name).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }
}