package com.cromp.executions.domain.repository;

import com.cromp.executions.domain.model.ExecutionAttempt;

import java.util.List;
import java.util.Optional;

public interface ExecutionAttemptRepositoryPort {
    ExecutionAttempt save(ExecutionAttempt attempt);
    Optional<ExecutionAttempt> findByIdAndOrganizationId(Long id, Long organizationId);
    List<ExecutionAttempt> findByExecutionId(Long executionId);
}
