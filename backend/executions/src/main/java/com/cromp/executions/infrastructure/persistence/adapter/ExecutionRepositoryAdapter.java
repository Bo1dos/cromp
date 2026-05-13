package com.cromp.executions.infrastructure.persistence.adapter;

import com.cromp.executions.domain.model.Execution;
import com.cromp.executions.domain.repository.ExecutionRepositoryPort;
import com.cromp.executions.infrastructure.persistence.jpa.repository.ExecutionJpaRepository;
import com.cromp.executions.infrastructure.persistence.mapper.ExecutionPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    public Optional<Execution> findByIdAndOrganizationId(Long id, Long organizationId) {
        return repository.findByIdAndOrganizationId(id, organizationId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Execution> findByExecUuidAndOrganizationId(UUID execUuid, Long organizationId) {
        return repository.findByExecUuidAndOrganizationId(execUuid, organizationId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Execution> findByOrganizationId(Long organizationId) {
        return repository.findByOrganizationIdOrderByCreatedAtDesc(organizationId).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Execution> findByJobId(Long organizationId, Long jobId) {
        return repository.findByOrganizationIdAndJobIdOrderByCreatedAtDesc(organizationId, jobId)
                .stream().map(mapper::toDomain).toList();
    }
}
