package com.cromp.iam.infrastructure.persistence.adapter;

import com.cromp.iam.domain.model.Organization;
import com.cromp.iam.domain.repository.OrganizationRepositoryPort;
import com.cromp.iam.infrastructure.persistence.jpa.repository.OrganizationJpaRepository;
import com.cromp.iam.infrastructure.persistence.mapper.OrganizationPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class OrganizationRepositoryAdapter implements OrganizationRepositoryPort {

    private final OrganizationJpaRepository repository;
    private final OrganizationPersistenceMapper mapper;

    @Override
    public Organization save(Organization organization) {
        return mapper.toDomain(repository.save(mapper.toJpa(organization)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Organization> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Organization> findByOrgUuid(UUID orgUuid) {
        return repository.findByOrgUuid(orgUuid).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Organization> findByName(String name) {
        return repository.findByName(name).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }
}