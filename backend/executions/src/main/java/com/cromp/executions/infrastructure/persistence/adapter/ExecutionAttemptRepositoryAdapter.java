package com.cromp.executions.infrastructure.persistence.adapter;

import com.cromp.executions.domain.model.ExecutionAttempt;
import com.cromp.executions.domain.repository.ExecutionAttemptRepositoryPort;
import com.cromp.executions.infrastructure.persistence.jpa.repository.ExecutionAttemptJpaRepository;
import com.cromp.executions.infrastructure.persistence.mapper.ExecutionAttemptPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class ExecutionAttemptRepositoryAdapter implements ExecutionAttemptRepositoryPort {
    private final ExecutionAttemptJpaRepository repository;
    private final ExecutionAttemptPersistenceMapper mapper;

    @Override
    public ExecutionAttempt save(ExecutionAttempt attempt) {
        return mapper.toDomain(repository.save(mapper.toJpa(attempt)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExecutionAttempt> findByIdAndOrganizationId(Long id, Long organizationId) {
        return repository.findByIdAndOrganizationId(id, organizationId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionAttempt> findByExecutionId(Long executionId) {
        return repository.findByExecutionIdOrderByAttemptNumberAsc(executionId).stream().map(mapper::toDomain).toList();
    }
}
