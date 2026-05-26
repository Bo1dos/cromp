package com.cromp.executions.domain.repository;

import com.cromp.executions.domain.model.Execution;
import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.enums.ExecutionStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecutionRepositoryPort {
    Execution save(Execution execution);
    Optional<Execution> findByExecUuid(UUID execUuid);
    Optional<Execution> findById(Long id);
    List<Execution> findByFilter(Long organizationId, Long jobId,
                                 ExecutionStatus status, ExecutionSource source,
                                 Instant from, Instant to,
                                 int page, int size);
    long countByFilter(Long organizationId, Long jobId,
                       ExecutionStatus status, ExecutionSource source,
                       Instant from, Instant to);
}