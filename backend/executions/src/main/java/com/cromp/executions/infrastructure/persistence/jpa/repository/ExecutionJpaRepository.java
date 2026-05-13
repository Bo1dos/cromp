package com.cromp.executions.infrastructure.persistence.jpa.repository;

import com.cromp.executions.infrastructure.persistence.jpa.entity.ExecutionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecutionJpaRepository extends JpaRepository<ExecutionJpaEntity, Long> {
    Optional<ExecutionJpaEntity> findByIdAndOrganizationId(Long id, Long organizationId);
    Optional<ExecutionJpaEntity> findByExecUuidAndOrganizationId(UUID execUuid, Long organizationId);
    List<ExecutionJpaEntity> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
    List<ExecutionJpaEntity> findByOrganizationIdAndJobIdOrderByCreatedAtDesc(Long organizationId, Long jobId);
}
