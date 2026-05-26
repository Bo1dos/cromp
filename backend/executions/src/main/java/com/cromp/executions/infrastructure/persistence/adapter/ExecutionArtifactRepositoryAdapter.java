package com.cromp.executions.infrastructure.persistence.adapter;

import com.cromp.executions.domain.model.ExecutionArtifact;
import com.cromp.executions.domain.repository.ExecutionArtifactRepositoryPort;
import com.cromp.executions.infrastructure.persistence.jpa.repository.ExecutionArtifactJpaRepository;
import com.cromp.executions.infrastructure.persistence.mapper.ExecutionArtifactPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class ExecutionArtifactRepositoryAdapter implements ExecutionArtifactRepositoryPort {

    private final ExecutionArtifactJpaRepository repository;
    private final ExecutionArtifactPersistenceMapper mapper;

    @Override
    public ExecutionArtifact save(ExecutionArtifact artifact) {
        return mapper.toDomain(repository.save(mapper.toJpa(artifact)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExecutionArtifact> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionArtifact> findByExecutionId(Long executionId) {
        return repository.findByExecutionIdOrderByUploadedAtAsc(executionId)
                .stream().map(mapper::toDomain).toList();
    }
}