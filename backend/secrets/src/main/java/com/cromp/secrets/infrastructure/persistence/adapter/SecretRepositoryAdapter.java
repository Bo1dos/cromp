package com.cromp.secrets.infrastructure.persistence.adapter;

import com.cromp.secrets.domain.model.Secret;
import com.cromp.secrets.domain.repository.SecretRepositoryPort;
import com.cromp.secrets.infrastructure.persistence.jpa.repository.SecretJpaRepository;
import com.cromp.secrets.infrastructure.persistence.mapper.SecretPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class SecretRepositoryAdapter implements SecretRepositoryPort {
    private final SecretJpaRepository repository;
    private final SecretPersistenceMapper mapper;

    @Override
    public Secret save(Secret secret) {
        return mapper.toDomain(repository.save(mapper.toJpa(secret)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Secret> findByIdAndOrganizationId(Long id, Long organizationId) {
        return repository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, organizationId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Secret> findByOrganizationId(Long organizationId) {
        return repository.findByOrganizationIdAndDeletedAtIsNullOrderByCreatedAtDesc(organizationId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndOrganizationId(String name, Long organizationId) {
        return repository.existsByNameAndOrganizationIdAndDeletedAtIsNull(name, organizationId);
    }
}
