package com.cromp.secrets.infrastructure.persistence.adapter;

import com.cromp.secrets.domain.model.SecretVersion;
import com.cromp.secrets.domain.repository.SecretVersionRepositoryPort;
import com.cromp.secrets.infrastructure.persistence.jpa.repository.SecretVersionJpaRepository;
import com.cromp.secrets.infrastructure.persistence.mapper.SecretVersionPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class SecretVersionRepositoryAdapter implements SecretVersionRepositoryPort {

    private final SecretVersionJpaRepository repository;
    private final SecretVersionPersistenceMapper mapper;

    @Override
    public SecretVersion save(SecretVersion version) {
        return mapper.toDomain(repository.save(mapper.toJpa(version)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SecretVersion> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SecretVersion> findBySecretIdAndVersion(Long secretId, int version) {
        return repository.findBySecretIdAndVersion(secretId, version).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SecretVersion> findActiveVersion(Long secretId) {
        return repository.findActiveVersion(secretId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SecretVersion> findBySecretIdOrderByVersionDesc(Long secretId) {
        return repository.findBySecretIdOrderByVersionDesc(secretId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> getMaxVersion(Long secretId) {
        return repository.getMaxVersion(secretId);
    }
}