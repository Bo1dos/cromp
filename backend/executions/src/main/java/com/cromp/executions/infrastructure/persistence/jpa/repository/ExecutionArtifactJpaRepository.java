package com.cromp.executions.infrastructure.persistence.jpa.repository;

import com.cromp.executions.infrastructure.persistence.jpa.entity.ExecutionArtifactJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExecutionArtifactJpaRepository extends JpaRepository<ExecutionArtifactJpaEntity, Long> {
    List<ExecutionArtifactJpaEntity> findByExecutionIdOrderByUploadedAtDesc(Long executionId);
}
