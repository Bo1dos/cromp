package com.cromp.executions.infrastructure.persistence.jpa.repository;

import com.cromp.executions.infrastructure.persistence.jpa.entity.ExecutionAttemptJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExecutionAttemptJpaRepository extends JpaRepository<ExecutionAttemptJpaEntity, Long> {
    Optional<ExecutionAttemptJpaEntity> findByIdAndOrganizationId(Long id, Long organizationId);
    List<ExecutionAttemptJpaEntity> findByExecutionIdOrderByAttemptNumberAsc(Long executionId);
}
