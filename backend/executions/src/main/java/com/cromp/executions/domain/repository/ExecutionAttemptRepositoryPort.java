package com.cromp.executions.domain.repository;

import com.cromp.executions.domain.model.ExecutionAttempt;
import com.cromp.executions.domain.model.enums.AttemptStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecutionAttemptRepositoryPort {
    ExecutionAttempt save(ExecutionAttempt attempt);
    Optional<ExecutionAttempt> findByAttemptUuid(UUID attemptUuid);
    Optional<ExecutionAttempt> findById(Long id);
    List<ExecutionAttempt> findByExecutionId(Long executionId);
    // Для janitor'а: найти зависшие попытки
    List<ExecutionAttempt> findByStatusAndUpdatedAtBefore(AttemptStatus status, Instant cutoff);
    // Для cancel: найти все незавершённые попытки execution'а
    List<ExecutionAttempt> findActiveByExecutionId(Long executionId);
    int findMaxAttemptNumberByExecutionId(Long executionId);
}