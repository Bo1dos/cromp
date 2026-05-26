package com.cromp.executions.infrastructure.persistence.adapter;

import com.cromp.executions.domain.model.ExecutionAttempt;
import com.cromp.executions.domain.model.enums.AttemptStatus;
import com.cromp.executions.domain.repository.ExecutionAttemptRepositoryPort;
import com.cromp.executions.infrastructure.persistence.jpa.repository.ExecutionAttemptJpaRepository;
import com.cromp.executions.infrastructure.persistence.mapper.ExecutionAttemptPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public Optional<ExecutionAttempt> findByAttemptUuid(UUID attemptUuid) {
        return repository.findByAttemptUuid(attemptUuid).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExecutionAttempt> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionAttempt> findByExecutionId(Long executionId) {
        return repository.findByExecutionIdOrderByAttemptNumberAsc(executionId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionAttempt> findByStatusAndUpdatedAtBefore(AttemptStatus status, Instant cutoff) {
        return repository.findByStatusAndUpdatedAtBefore(status, cutoff)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionAttempt> findActiveByExecutionId(Long executionId) {
        return repository.findActiveByExecutionId(executionId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public int findMaxAttemptNumberByExecutionId(Long executionId) {
        return repository.findMaxAttemptNumberByExecutionId(executionId);
    }
}