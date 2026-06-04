package com.cromp.executions.application.service;

import com.cromp.executions.api.dto.ExecutionFilter;
import com.cromp.executions.api.dto.request.CreateExecutionRequest;
import com.cromp.executions.api.dto.response.ExecutionDetailResponse;
import com.cromp.executions.api.dto.response.ExecutionResponse;
import com.cromp.executions.api.dto.response.PagedResponse;
import com.cromp.executions.api.mapper.ExecutionApiMapper;
import com.cromp.executions.application.port.AuditPort;
import com.cromp.executions.application.port.JobVersionQueryPort;
import com.cromp.executions.domain.model.Execution;
import com.cromp.executions.domain.model.ExecutionAttempt;
import com.cromp.executions.domain.model.enums.AttemptStatus;
import com.cromp.executions.domain.model.enums.ExecutionStatus;
import com.cromp.executions.domain.model.exceptions.ExecutionNotFoundException;
import com.cromp.executions.domain.repository.ExecutionAttemptRepositoryPort;
import com.cromp.executions.domain.repository.ExecutionRepositoryPort;
import com.cromp.executions.domain.service.AttemptStateMachine;
import com.cromp.executions.domain.service.ExecutionStateMachine;
import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ExecutionService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionService.class);

    private final ExecutionRepositoryPort executionRepository;
    private final ExecutionAttemptRepositoryPort attemptRepository;
    private final JobVersionQueryPort jobVersionQueryPort;
    private final AuditPort auditPort;
    private final ExecutionApiMapper mapper;
    private final CurrentActorPort currentActorPort;
    private final PermissionCheckerPort permissionCheckerPort;

    // Вызывается через ExecutionCreationPort (ExecutionCreationPortImpl)
    public UUID createExecution(CreateExecutionRequest request) {
        String policySnapshot = request.executionPolicySnapshot() != null
                ? request.executionPolicySnapshot()
                : jobVersionQueryPort.findConfigJsonByVersionId(request.jobVersionId())
                        .orElse(null);

        Execution execution = Execution.create(
                request.organizationId(),
                request.jobId(),
                request.jobVersionId(),
                request.priority(),
                request.source(),
                request.scheduledAt(),
                request.correlationId(),
                policySnapshot
        );
        execution = executionRepository.save(execution);

        // Первая попытка создаётся сразу - триггер БД обновит totalAttempts и статус
        int nextAttemptNumber = 1;
        ExecutionAttempt attempt = ExecutionAttempt.create(
                execution.getId(),
                request.organizationId(),
                nextAttemptNumber,
                request.scheduledAt() != null ? request.scheduledAt() : Instant.now(),
                request.correlationId() != null ? request.correlationId() : UUID.randomUUID()
        );
        attemptRepository.save(attempt);

        auditPort.record("EXECUTION.CREATE", request.organizationId(),
                request.triggeredBy(), "executions", execution.getId(),
                Map.of("jobId", request.jobId(), "source", request.source().name()));

        return execution.getExecUuid();
    }

    @Transactional(readOnly = true)
    public ExecutionDetailResponse getExecution(Long organizationId, UUID execUuid) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.isMember(userId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }

        Execution execution = executionRepository.findByExecUuid(execUuid)
                .filter(e -> e.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new ExecutionNotFoundException(execUuid));

        List<ExecutionAttempt> attempts = attemptRepository.findByExecutionId(execution.getId());
        return mapper.toDetailResponse(execution, attempts);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ExecutionResponse> listExecutions(Long organizationId, ExecutionFilter filter) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.isMember(userId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }

        try {
            List<ExecutionResponse> items = executionRepository
                    .findByFilter(organizationId, filter.jobId(), filter.status(),
                            filter.source(), filter.from(), filter.to(),
                            filter.page(), filter.size())
                    .stream()
                    .map(mapper::toResponse)
                    .toList();

            long total = executionRepository.countByFilter(organizationId, filter.jobId(),
                    filter.status(), filter.source(), filter.from(), filter.to());

            return new PagedResponse<>(items, filter.page(), filter.size(), total);
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to list executions for orgId={}, filter={}: {}", 
                    organizationId, filter, e.getMessage(), e);
            return new PagedResponse<>(Collections.emptyList(), filter.page(), filter.size(), 0);
        }
    }

    public void cancelExecution(Long organizationId, UUID execUuid) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "job:execute")) {
            throw new SecurityException("No permission to cancel executions");
        }

        Execution execution = executionRepository.findByExecUuid(execUuid)
                .filter(e -> e.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new ExecutionNotFoundException(execUuid));

        ExecutionStateMachine.assertTransitionAllowed(execution.getFinalStatus(), ExecutionStatus.CANCELLED);

        // Отменяем все незавершённые попытки
        List<ExecutionAttempt> active = attemptRepository.findActiveByExecutionId(execution.getId());
        for (ExecutionAttempt attempt : active) {
            AttemptStateMachine.assertTransitionAllowed(attempt.getStatus(), AttemptStatus.CANCELLED);
            attempt.cancel("Execution cancelled by user");
            attemptRepository.save(attempt);
        }

        execution.cancel();
        executionRepository.save(execution);

        auditPort.record("EXECUTION.CANCEL", organizationId, userId,
                "executions", execution.getId(), Map.of());
    }
}