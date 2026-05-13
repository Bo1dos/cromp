package com.cromp.executions.api.service;

import com.cromp.executions.api.dto.request.CreateExecutionArtifactRequest;
import com.cromp.executions.api.dto.request.CreateExecutionRequest;
import com.cromp.executions.api.dto.request.UpdateAttemptStatusRequest;
import com.cromp.executions.api.dto.response.ExecutionArtifactResponse;
import com.cromp.executions.api.dto.response.ExecutionAttemptResponse;
import com.cromp.executions.api.dto.response.ExecutionResponse;

import java.util.List;

public interface ExecutionFacade {
    ExecutionResponse createExecution(Long organizationId, CreateExecutionRequest request);
    ExecutionResponse getExecution(Long organizationId, Long executionId);
    List<ExecutionResponse> listExecutions(Long organizationId, Long jobId);
    void cancelExecution(Long organizationId, Long executionId);
    List<ExecutionAttemptResponse> listAttempts(Long organizationId, Long executionId);
    ExecutionAttemptResponse updateAttemptStatus(Long organizationId, Long attemptId, UpdateAttemptStatusRequest request);
    ExecutionArtifactResponse addArtifact(Long organizationId, Long executionId, CreateExecutionArtifactRequest request);
    List<ExecutionArtifactResponse> listArtifacts(Long organizationId, Long executionId);
}
