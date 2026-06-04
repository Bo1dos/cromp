package com.cromp.executions.infrastructure.persistence.jpa.repository;

import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.enums.ExecutionStatus;
import com.cromp.executions.infrastructure.persistence.jpa.entity.ExecutionJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecutionJpaRepository extends JpaRepository<ExecutionJpaEntity, Long> {

    Optional<ExecutionJpaEntity> findByExecUuid(UUID execUuid);

    @Query("""
            SELECT e FROM ExecutionJpaEntity e
            WHERE e.organizationId = :orgId
              AND (:jobId  IS NULL OR e.jobId      = :jobId)
              AND (cast(:status as String) IS NULL OR e.finalStatus = :status)
              AND (cast(:source as String) IS NULL OR e.source      = :source)
              AND (cast(:from as Instant) IS NULL OR e.createdAt  >= :from)
              AND (cast(:to as Instant) IS NULL OR e.createdAt  <= :to)
            ORDER BY e.createdAt DESC
            """)
    List<ExecutionJpaEntity> findByFilter(
            @Param("orgId")  Long organizationId,
            @Param("jobId")  Long jobId,
            @Param("status") ExecutionStatus status,
            @Param("source") ExecutionSource source,
            @Param("from")   Instant from,
            @Param("to")     Instant to,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(e) FROM ExecutionJpaEntity e
            WHERE e.organizationId = :orgId
              AND (:jobId  IS NULL OR e.jobId       = :jobId)
              AND (cast(:status as String) IS NULL OR e.finalStatus  = :status)
              AND (cast(:source as String) IS NULL OR e.source       = :source)
              AND (cast(:from as Instant) IS NULL OR e.createdAt   >= :from)
              AND (cast(:to as Instant) IS NULL OR e.createdAt   <= :to)
            """)
    long countByFilter(
            @Param("orgId")  Long organizationId,
            @Param("jobId")  Long jobId,
            @Param("status") ExecutionStatus status,
            @Param("source") ExecutionSource source,
            @Param("from")   Instant from,
            @Param("to")     Instant to
    );
}