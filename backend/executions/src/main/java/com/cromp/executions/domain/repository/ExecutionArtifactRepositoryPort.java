package com.cromp.executions.domain.repository;

import com.cromp.executions.domain.model.ExecutionArtifact;

import java.util.List;
import java.util.Optional;

public interface ExecutionArtifactRepositoryPort {
    ExecutionArtifact save(ExecutionArtifact artifact);
    Optional<ExecutionArtifact> findById(Long id);
    List<ExecutionArtifact> findByExecutionId(Long executionId);
}