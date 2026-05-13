package com.cromp.secrets.infrastructure.persistence.adapter;

import com.cromp.secrets.domain.model.ExecutionSecretAccess;
import com.cromp.secrets.domain.repository.ExecutionSecretAccessRepositoryPort;
import com.cromp.secrets.infrastructure.persistence.jpa.repository.ExecutionSecretAccessJpaRepository;
import com.cromp.secrets.infrastructure.persistence.mapper.ExecutionSecretAccessPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ExecutionSecretAccessRepositoryAdapter implements ExecutionSecretAccessRepositoryPort {
    private final ExecutionSecretAccessJpaRepository repository;
    private final ExecutionSecretAccessPersistenceMapper mapper;

    @Override
    public ExecutionSecretAccess save(ExecutionSecretAccess access) {
        return mapper.toDomain(repository.save(mapper.toJpa(access)));
    }
}
