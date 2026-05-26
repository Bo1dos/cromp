package com.cromp.executions.infrastructure.persistence.jpa.repository;

import com.cromp.executions.domain.model.enums.AttemptStatus;
import com.cromp.executions.infrastructure.persistence.jpa.entity.ExecutionAttemptJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecutionAttemptJpaRepository extends JpaRepository<ExecutionAttemptJpaEntity, Long> {

    Optional<ExecutionAttemptJpaEntity> findByAttemptUuid(UUID attemptUuid);

    List<ExecutionAttemptJpaEntity> findByExecutionIdOrderByAttemptNumberAsc(Long executionId);

    List<ExecutionAttemptJpaEntity> findByStatusAndUpdatedAtBefore(AttemptStatus status, Instant cutoff);

    @Query("""
            SELECT a FROM ExecutionAttemptJpaEntity a
            WHERE a.executionId = :executionId
              AND a.status NOT IN (
                com.cromp.executions.domain.model.enums.AttemptStatus.SUCCEEDED,
                com.cromp.executions.domain.model.enums.AttemptStatus.FAILED,
                com.cromp.executions.domain.model.enums.AttemptStatus.TIMEOUT,
                com.cromp.executions.domain.model.enums.AttemptStatus.CANCELLED
              )
            """)
    List<ExecutionAttemptJpaEntity> findActiveByExecutionId(@Param("executionId") Long executionId);

    @Query("SELECT COALESCE(MAX(a.attemptNumber), 0) FROM ExecutionAttemptJpaEntity a WHERE a.executionId = :executionId")
    int findMaxAttemptNumberByExecutionId(@Param("executionId") Long executionId);

    // Для listAttempts по execUuid через join
    @Query("""
            SELECT a FROM ExecutionAttemptJpaEntity a
            JOIN ExecutionJpaEntity e ON e.id = a.executionId
            WHERE e.execUuid = :execUuid
              AND e.organizationId = :orgId
            ORDER BY a.attemptNumber ASC
            """)
    List<ExecutionAttemptJpaEntity> findByExecUuidAndOrgId(
            @Param("execUuid") UUID execUuid,
            @Param("orgId")    Long organizationId
    );
}