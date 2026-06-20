package com.cromp.executions.application.service;

import com.cromp.executions.api.dto.request.CompleteAttemptRequest;
import com.cromp.executions.api.dto.response.AttemptResponse;
import com.cromp.executions.api.dto.response.ClaimAttemptResult;
import com.cromp.executions.api.mapper.ExecutionApiMapper;
import com.cromp.executions.application.port.ArtifactStoragePort;
import com.cromp.executions.application.port.AuditPort;
import com.cromp.executions.application.port.StoredArtifact;
import com.cromp.executions.domain.model.ExecutionArtifact;
import com.cromp.executions.domain.model.ExecutionAttempt;
import com.cromp.executions.domain.model.enums.ArtifactKind;
import com.cromp.executions.domain.model.enums.AttemptStatus;
import com.cromp.executions.domain.model.exceptions.AttemptNotFoundException;
import com.cromp.executions.domain.model.exceptions.InvalidStatusTransitionException;
import com.cromp.executions.domain.repository.ExecutionArtifactRepositoryPort;
import com.cromp.executions.domain.repository.ExecutionAttemptRepositoryPort;
import com.cromp.executions.domain.service.AttemptStateMachine;
import com.cromp.executions.infrastructure.persistence.custom.ExecutionAttemptCustomRepository;
import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AttemptService {

    // Терминальные статусы — не трогаем при отмене
    private static final Set<AttemptStatus> TERMINAL =
            Set.of(AttemptStatus.SUCCEEDED, AttemptStatus.FAILED,
                   AttemptStatus.TIMEOUT, AttemptStatus.CANCELLED);

    private final ExecutionAttemptRepositoryPort attemptRepository;
    private final ExecutionArtifactRepositoryPort artifactRepository;
    private final ArtifactStoragePort artifactStorage;
    private final AuditPort auditPort;
    private final ExecutionAttemptCustomRepository customRepository;
    private final ExecutionApiMapper mapper;
    private final CurrentActorPort currentActorPort;
    private final PermissionCheckerPort permissionCheckerPort;

    // Вызывается через AttemptClaimPort
    public Optional<ClaimAttemptResult> claimNextAttempt(Long organizationId) {
        return customRepository.claimAttempt(organizationId);
    }

    // Вызывается через AttemptCompletionPort — executor отметил, что начал выполнение
    public void markRunning(UUID attemptUuid) {
        ExecutionAttempt attempt = attemptRepository.findByAttemptUuid(attemptUuid)
                .orElseThrow(() -> new AttemptNotFoundException(attemptUuid));
        AttemptStateMachine.assertTransitionAllowed(attempt.getStatus(), AttemptStatus.RUNNING);
        attempt.start(null);
        attemptRepository.save(attempt);
    }

    // Вызывается через AttemptCompletionPort
    public void completeAttempt(UUID attemptUuid, CompleteAttemptRequest request) {
        AttemptStatus newStatus = request.status();
        if (!TERMINAL.contains(newStatus)) {
            throw new InvalidStatusTransitionException(
                    "completeAttempt accepts only terminal statuses, got: " + newStatus);
        }

        ExecutionAttempt attempt = attemptRepository.findByAttemptUuid(attemptUuid)
                .orElseThrow(() -> new AttemptNotFoundException(attemptUuid));

        AttemptStateMachine.assertTransitionAllowed(attempt.getStatus(), newStatus);
        attempt.complete(newStatus, request.outputSummary(),
                         request.errorClass(), request.statusReason());
        attemptRepository.save(attempt);

        // Авто-сохранение бинарного ответа как артефакта
        if (newStatus == AttemptStatus.SUCCEEDED
                && request.rawBody() != null && request.rawBody().length > 0) {
            try {
                saveArtifactFromAttempt(attempt, request.rawBody(), request.contentType());
            } catch (Exception e) {
                log.error("Failed to auto-save artifact for attempt {}", attemptUuid, e);
                // Не фейлим попытку — артефакт опционален
            }
        }

        // Аудит завершения выполнения
        String auditAction = switch (newStatus) {
            case SUCCEEDED -> "EXECUTION.SUCCEED";
            case FAILED -> "EXECUTION.FAIL";
            case TIMEOUT -> "EXECUTION.TIMEOUT";
            case CANCELLED -> "EXECUTION.CANCELLED";
            default -> null;
        };
        if (auditAction != null) {
            auditPort.record(auditAction, attempt.getOrganizationId(), null,
                    "executions", attempt.getExecutionId(),
                    java.util.Map.of("attemptUuid", attempt.getAttemptUuid()));
        }
        // Обновление finalStatus Execution — через триггер БД (fn_update_execution_final_status)
    }

    private void saveArtifactFromAttempt(ExecutionAttempt attempt,
                                         byte[] data, String contentType) {
        String filename = "response-" + attempt.getAttemptUuid() + ".bin";
        StoredArtifact stored = artifactStorage.store(
                attempt.getExecutionId(), filename,
                contentType != null ? contentType : "application/octet-stream",
                new ByteArrayInputStream(data), data.length);

        ExecutionArtifact artifact = ExecutionArtifact.create(
                attempt.getExecutionId(),
                ArtifactKind.OUTPUT_PAYLOAD,
                stored.storagePath(),
                stored.sizeBytes(),
                stored.checksumSha256(),
                contentType != null ? contentType : "application/octet-stream",
                null  // uploadedBy — системный артефакт
        );
        artifactRepository.save(artifact);
        log.info("Auto-saved artifact id={} for attempt {}", artifact.getId(), attempt.getAttemptUuid());
    }

    @Transactional(readOnly = true)
    public List<AttemptResponse> listAttempts(Long organizationId, UUID execUuid) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.isMember(userId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }
        // executionId резолвится в репозитории через execUuid + organizationId
        return customRepository.findAttemptsByExecUuid(organizationId, execUuid)
                .stream()
                .map(mapper::toAttemptResponse)
                .toList();
    }

    // Для janitor'а (вызывается из orchestrator)
    @Transactional(readOnly = true)
    public List<ExecutionAttempt> findStaleAttempts(int olderThanMinutes) {
        Instant cutoff = Instant.now().minusSeconds((long) olderThanMinutes * 60);
        return attemptRepository.findByStatusAndUpdatedAtBefore(AttemptStatus.RUNNING, cutoff);
    }

    public void markStaleAsTimeout(List<ExecutionAttempt> stale) {
        for (ExecutionAttempt attempt : stale) {
            AttemptStateMachine.assertTransitionAllowed(attempt.getStatus(), AttemptStatus.TIMEOUT);
            attempt.markTimeout();
            attemptRepository.save(attempt);
            // Триггер обновит Execution.finalStatus = FAILED
        }
    }
}