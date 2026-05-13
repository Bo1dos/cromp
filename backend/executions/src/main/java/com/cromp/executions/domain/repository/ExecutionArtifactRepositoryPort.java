package com.cromp.executions.domain.repository;

import com.cromp.executions.domain.model.ExecutionArtifact;

import java.util.List;

public interface ExecutionArtifactRepositoryPort {
    ExecutionArtifact save(ExecutionArtifact artifact);
    List<ExecutionArtifact> findByExecutionId(Long executionId);
}
