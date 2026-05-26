package com.cromp.executions.infrastructure.persistence.adapter;

import com.cromp.executions.domain.model.Execution;
import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.enums.ExecutionStatus;
import com.cromp.executions.domain.repository.ExecutionRepositoryPort;
import com.cromp.executions.infrastructure.persistence.jpa.repository.ExecutionJpaRepository;
import com.cromp.executions.infrastructure.persistence.mapper.ExecutionPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class ExecutionRepositoryAdapter implements ExecutionRepositoryPort {

    private final ExecutionJpaRepository repository;
    private final ExecutionPersistenceMapper mapper;

    @Override
    public Execution save(Execution execution) {
        return mapper.toDomain(repository.save(mapper.toJpa(execution)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Execution> findByExecUuid(UUID execUuid) {
        return repository.findByExecUuid(execUuid).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Execution> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Execution> findByFilter(Long organizationId, Long jobId,
                                        ExecutionStatus status, ExecutionSource source,
                                        Instant from, Instant to, int page, int size) {
        return repository.findByFilter(organizationId, jobId, status, source, from, to,
                        PageRequest.of(page, size))
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByFilter(Long organizationId, Long jobId,
                               ExecutionStatus status, ExecutionSource source,
                               Instant from, Instant to) {
        return repository.countByFilter(organizationId, jobId, status, source, from, to);
    }
}