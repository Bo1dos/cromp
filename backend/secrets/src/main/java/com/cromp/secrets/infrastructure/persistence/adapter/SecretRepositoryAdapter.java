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
import java.util.UUID;

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
    public Optional<Secret> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Secret> findBySecretUuid(UUID secretUuid) {
        return repository.findBySecretUuid(secretUuid).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Secret> findByNameAndOrganizationId(String name, Long organizationId) {
        return repository.findByNameAndOrganizationIdAndDeletedAtIsNull(name, organizationId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Secret> findByOrganizationId(Long organizationId) {
        return repository.findByOrganizationIdAndDeletedAtIsNull(organizationId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public void delete(Secret secret) {
        repository.delete(mapper.toJpa(secret));
    }
}