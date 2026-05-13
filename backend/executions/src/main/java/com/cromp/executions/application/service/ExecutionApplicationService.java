package com.cromp.executions.application.service;

import com.cromp.executions.api.dto.request.CreateExecutionArtifactRequest;
import com.cromp.executions.api.dto.request.CreateExecutionRequest;
import com.cromp.executions.api.dto.request.UpdateAttemptStatusRequest;
import com.cromp.executions.api.dto.response.ExecutionArtifactResponse;
import com.cromp.executions.api.dto.response.ExecutionAttemptResponse;
import com.cromp.executions.api.dto.response.ExecutionResponse;
import com.cromp.executions.api.mapper.ExecutionApiMapper;
import com.cromp.executions.api.service.ExecutionFacade;
import com.cromp.executions.domain.model.Execution;
import com.cromp.executions.domain.model.ExecutionArtifact;
import com.cromp.executions.domain.model.ExecutionAttempt;
import com.cromp.executions.domain.model.enums.ArtifactKind;
import com.cromp.executions.domain.model.enums.AttemptStatus;
import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.exceptions.ExecutionAttemptNotFoundException;
import com.cromp.executions.domain.model.exceptions.ExecutionNotFoundException;
import com.cromp.executions.domain.repository.ExecutionArtifactRepositoryPort;
import com.cromp.executions.domain.repository.ExecutionAttemptRepositoryPort;
import com.cromp.executions.domain.repository.ExecutionRepositoryPort;
import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ExecutionApplicationService implements ExecutionFacade {
    private final ExecutionRepositoryPort executionRepository;
    private final ExecutionAttemptRepositoryPort attemptRepository;
    private final ExecutionArtifactRepositoryPort artifactRepository;
    private final CurrentActorPort currentActorPort;
    private final PermissionCheckerPort permissionCheckerPort;
    private final ExecutionApiMapper mapper;

    @Override
    public ExecutionResponse createExecution(Long organizationId, CreateExecutionRequest request) {
        Long userId = requireUser();
        requirePermission(userId, organizationId, "job:execute");
        Execution execution = createExecutionDomain(
                organizationId,
                request.jobId(),
                request.jobVersionId(),
                request.priority() != null ? request.priority() : 0,
                ExecutionSource.valueOf(request.source()),
                request.scheduledAt(),
                request.correlationId(),
                request.executionPolicySnapshot()
        );
        return mapper.toResponse(execution);
    }

    public Execution createExecutionDomain(Long organizationId, Long jobId, Long jobVersionId, int priority,
                                           ExecutionSource source, Instant scheduledAt, UUID correlationId,
                                           Map<String, Object> policySnapshot) {
        Execution execution = executionRepository.save(Execution.create(UUID.randomUUID(), organizationId, jobId,
                jobVersionId, priority, source, scheduledAt,
                correlationId != null ? correlationId : UUID.randomUUID(),
                policySnapshot != null ? policySnapshot : Map.of()));
        attemptRepository.save(ExecutionAttempt.create(execution.getId(), organizationId, 1, scheduledAt));
        return execution;
    }

    @Override
    @Transactional(readOnly = true)
    public ExecutionResponse getExecution(Long organizationId, Long executionId) {
        requireMember(requireUser(), organizationId);
        return mapper.toResponse(requireExecution(organizationId, executionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionResponse> listExecutions(Long organizationId, Long jobId) {
        requireMember(requireUser(), organizationId);
        List<Execution> executions = jobId == null
                ? executionRepository.findByOrganizationId(organizationId)
                : executionRepository.findByJobId(organizationId, jobId);
        return executions.stream().map(mapper::toResponse).toList();
    }

    @Override
    public void cancelExecution(Long organizationId, Long executionId) {
        Long userId = requireUser();
        requirePermission(userId, organizationId, "job:execute");
        Execution execution = requireExecution(organizationId, executionId);
        execution.cancel();
        executionRepository.save(execution);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionAttemptResponse> listAttempts(Long organizationId, Long executionId) {
        requireMember(requireUser(), organizationId);
        requireExecution(organizationId, executionId);
        return attemptRepository.findByExecutionId(executionId).stream().map(mapper::toAttemptResponse).toList();
    }

    @Override
    public ExecutionAttemptResponse updateAttemptStatus(Long organizationId, Long attemptId, UpdateAttemptStatusRequest request) {
        Long userId = requireUser();
        requirePermission(userId, organizationId, "job:execute");
        ExecutionAttempt attempt = attemptRepository.findByIdAndOrganizationId(attemptId, organizationId)
                .orElseThrow(() -> new ExecutionAttemptNotFoundException(attemptId));
        attempt.changeStatus(AttemptStatus.valueOf(request.status()), request.statusReason(),
                request.errorClass(), request.outputSummary());
        return mapper.toAttemptResponse(attemptRepository.save(attempt));
    }

    @Override
    public ExecutionArtifactResponse addArtifact(Long organizationId, Long executionId, CreateExecutionArtifactRequest request) {
        Long userId = requireUser();
        requirePermission(userId, organizationId, "job:execute");
        requireExecution(organizationId, executionId);
        ExecutionArtifact artifact = ExecutionArtifact.builder()
                .executionId(executionId)
                .kind(ArtifactKind.valueOf(request.kind()))
                .storagePath(request.storagePath())
                .sizeBytes(request.sizeBytes())
                .checksumSha256(request.checksumSha256())
                .contentType(request.contentType() != null ? request.contentType() : "text/plain")
                .compression(request.compression())
                .metadata(request.metadata() != null ? request.metadata() : Map.of())
                .retentionDays(request.retentionDays())
                .uploadedBy(userId)
                .uploadedAt(Instant.now())
                .build();
        return mapper.toArtifactResponse(artifactRepository.save(artifact));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionArtifactResponse> listArtifacts(Long organizationId, Long executionId) {
        requireMember(requireUser(), organizationId);
        requireExecution(organizationId, executionId);
        return artifactRepository.findByExecutionId(executionId).stream().map(mapper::toArtifactResponse).toList();
    }

    private Execution requireExecution(Long organizationId, Long executionId) {
        return executionRepository.findByIdAndOrganizationId(executionId, organizationId)
                .orElseThrow(() -> new ExecutionNotFoundException(executionId));
    }

    private Long requireUser() {
        return currentActorPort.currentUserId().orElseThrow(() -> new SecurityException("Not authenticated"));
    }

    private void requireMember(Long userId, Long organizationId) {
        if (!permissionCheckerPort.isMember(userId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }
    }

    private void requirePermission(Long userId, Long organizationId, String permission) {
        if (!permissionCheckerPort.hasPermission(userId, organizationId, permission)) {
            throw new SecurityException("No permission: " + permission);
        }
    }
}
