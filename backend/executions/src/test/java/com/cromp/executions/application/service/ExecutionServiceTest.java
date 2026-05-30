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
import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.enums.ExecutionStatus;
import com.cromp.executions.domain.model.exceptions.ExecutionNotFoundException;
import com.cromp.executions.domain.model.exceptions.InvalidStatusTransitionException;
import com.cromp.executions.domain.repository.ExecutionAttemptRepositoryPort;
import com.cromp.executions.domain.repository.ExecutionRepositoryPort;
import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExecutionServiceTest {

    private static final Long USER_ID = 11L;
    private static final Long ORGANIZATION_ID = 22L;
    private static final Long JOB_ID = 33L;
    private static final Long JOB_VERSION_ID = 44L;

    @Mock private ExecutionRepositoryPort executionRepository;
    @Mock private ExecutionAttemptRepositoryPort attemptRepository;
    @Mock private JobVersionQueryPort jobVersionQueryPort;
    @Mock private AuditPort auditPort;
    @Mock private CurrentActorPort currentActorPort;
    @Mock private PermissionCheckerPort permissionCheckerPort;

    private ExecutionService service;

    @BeforeEach
    void setUp() {
        service = new ExecutionService(
                executionRepository,
                attemptRepository,
                jobVersionQueryPort,
                auditPort,
                new ExecutionApiMapper(),
                currentActorPort,
                permissionCheckerPort
        );
    }

    @Test
    void shouldCreateExecutionWithSnapshotWithoutQueryingJobVersionPort() {
        UUID correlationId = UUID.randomUUID();
        CreateExecutionRequest request = new CreateExecutionRequest(
                ORGANIZATION_ID, JOB_ID, JOB_VERSION_ID, 5, ExecutionSource.MANUAL,
                USER_ID, Instant.parse("2024-01-01T10:00:00Z"), correlationId,
                Map.of("payload", true), "{\"policy\":true}"
        );

        when(executionRepository.save(any(Execution.class))).thenAnswer(invocation -> persistedExecution(invocation.getArgument(0), 100L));
        when(attemptRepository.save(any(ExecutionAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID execUuid = service.createExecution(request);

        ArgumentCaptor<Execution> executionCaptor = ArgumentCaptor.forClass(Execution.class);
        verify(executionRepository).save(executionCaptor.capture());
        assertThat(executionCaptor.getValue().getFinalStatus()).isEqualTo(ExecutionStatus.CREATED);
        assertThat(executionCaptor.getValue().getExecutionPolicySnapshot()).isEqualTo("{\"policy\":true}");
        assertThat(executionCaptor.getValue().getCorrelationId()).isEqualTo(correlationId);

        ArgumentCaptor<ExecutionAttempt> attemptCaptor = ArgumentCaptor.forClass(ExecutionAttempt.class);
        verify(attemptRepository).save(attemptCaptor.capture());
        assertThat(attemptCaptor.getValue().getAttemptNumber()).isEqualTo(1);
        assertThat(attemptCaptor.getValue().getScheduledAt()).isEqualTo(request.scheduledAt());

        verifyNoInteractions(jobVersionQueryPort);
        verify(auditPort).record(
                eq("EXECUTION.CREATE"), eq(ORGANIZATION_ID), eq(USER_ID), eq("executions"), eq(100L),
                argThat((Map<String, Object> map) ->
                        JOB_ID.equals(map.get("jobId"))
                                && ExecutionSource.MANUAL.name().equals(map.get("source")))
        );
        assertThat(execUuid).isEqualTo(executionCaptor.getValue().getExecUuid());
    }

    @Test
    void shouldFallbackToJobVersionSnapshotWhenSnapshotIsMissing() {
        when(jobVersionQueryPort.findConfigJsonByVersionId(JOB_VERSION_ID)).thenReturn(Optional.of("{\"job\":true}"));
        when(executionRepository.save(any(Execution.class))).thenAnswer(invocation -> persistedExecution(invocation.getArgument(0), 100L));
        when(attemptRepository.save(any(ExecutionAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID execUuid = service.createExecution(new CreateExecutionRequest(
                ORGANIZATION_ID, JOB_ID, JOB_VERSION_ID, 5, ExecutionSource.SCHEDULED,
                USER_ID, null, null, Map.of(), null
        ));

        verify(jobVersionQueryPort).findConfigJsonByVersionId(JOB_VERSION_ID);
        ArgumentCaptor<Execution> executionCaptor = ArgumentCaptor.forClass(Execution.class);
        verify(executionRepository).save(executionCaptor.capture());
        assertThat(executionCaptor.getValue().getExecutionPolicySnapshot()).isEqualTo("{\"job\":true}");
        assertThat(execUuid).isEqualTo(executionCaptor.getValue().getExecUuid());
    }

    @Test
    void shouldCreateExecutionEvenWhenSnapshotAndJobVersionConfigAreMissing() {
        when(jobVersionQueryPort.findConfigJsonByVersionId(JOB_VERSION_ID)).thenReturn(Optional.empty());
        when(executionRepository.save(any(Execution.class))).thenAnswer(invocation -> persistedExecution(invocation.getArgument(0), 100L));
        when(attemptRepository.save(any(ExecutionAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createExecution(new CreateExecutionRequest(
                ORGANIZATION_ID, JOB_ID, JOB_VERSION_ID, 0, ExecutionSource.API,
                USER_ID, null, null, Map.of(), null
        ));

        ArgumentCaptor<Execution> executionCaptor = ArgumentCaptor.forClass(Execution.class);
        verify(executionRepository).save(executionCaptor.capture());
        assertThat(executionCaptor.getValue().getExecutionPolicySnapshot()).isNull();
    }

    @Test
    void shouldReturnExecutionDetailsWhenMemberAndExecutionBelongsToOrganization() {
        Execution execution = persistedExecution(Execution.create(ORGANIZATION_ID, JOB_ID, JOB_VERSION_ID, 1, ExecutionSource.MANUAL, null, UUID.randomUUID(), null), 100L);
        ExecutionAttempt attempt = persistedAttempt(ExecutionAttempt.create(100L, ORGANIZATION_ID, 1, Instant.parse("2024-01-01T00:00:00Z"), null), 200L);
        authenticate();
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(true);
        when(executionRepository.findByExecUuid(execution.getExecUuid())).thenReturn(Optional.of(execution));
        when(attemptRepository.findByExecutionId(100L)).thenReturn(List.of(attempt));

        ExecutionDetailResponse response = service.getExecution(ORGANIZATION_ID, execution.getExecUuid());

        assertThat(response.execUuid()).isEqualTo(execution.getExecUuid());
        assertThat(response.attempts()).hasSize(1);
        assertThat(response.attempts().getFirst().attemptUuid()).isEqualTo(attempt.getAttemptUuid());
    }

    @Test
    void shouldThrowWhenGettingExecutionWithoutAuthentication() {
        when(currentActorPort.currentUserId()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getExecution(ORGANIZATION_ID, UUID.randomUUID()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Not authenticated");
    }

    @Test
    void shouldThrowWhenGettingExecutionWithoutMembership() {
        authenticate();
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.getExecution(ORGANIZATION_ID, UUID.randomUUID()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Not a member");
    }

    @Test
    void shouldThrowWhenExecutionDoesNotExist() {
        authenticate();
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(true);

        UUID execUuid = UUID.randomUUID();
        when(executionRepository.findByExecUuid(execUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getExecution(ORGANIZATION_ID, execUuid))
                .isInstanceOf(ExecutionNotFoundException.class);
    }

    @Test
    void shouldThrowWhenExecutionBelongsToDifferentOrganization() {
        authenticate();
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(true);
        Execution execution = persistedExecution(Execution.create(ORGANIZATION_ID + 1, JOB_ID, JOB_VERSION_ID, 1, ExecutionSource.API, null, UUID.randomUUID(), null), 100L);
        when(executionRepository.findByExecUuid(execution.getExecUuid())).thenReturn(Optional.of(execution));

        assertThatThrownBy(() -> service.getExecution(ORGANIZATION_ID, execution.getExecUuid()))
                .isInstanceOf(ExecutionNotFoundException.class);
    }

    @Test
    void shouldListExecutionsWithFiltersAndPagination() {
        authenticate();
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(true);
        Execution first = persistedExecution(Execution.create(ORGANIZATION_ID, JOB_ID, JOB_VERSION_ID, 1, ExecutionSource.SCHEDULED, null, UUID.randomUUID(), null), 100L);
        Execution second = persistedExecution(Execution.create(ORGANIZATION_ID, JOB_ID, JOB_VERSION_ID, 2, ExecutionSource.API, null, UUID.randomUUID(), null), 101L);
        when(executionRepository.findByFilter(eq(ORGANIZATION_ID), eq(JOB_ID), eq(ExecutionStatus.CREATED), eq(ExecutionSource.API),
                eq(Instant.parse("2024-01-01T00:00:00Z")), eq(Instant.parse("2024-01-02T00:00:00Z")), eq(2), eq(10)))
                .thenReturn(List.of(first, second));
        when(executionRepository.countByFilter(eq(ORGANIZATION_ID), eq(JOB_ID), eq(ExecutionStatus.CREATED), eq(ExecutionSource.API),
                eq(Instant.parse("2024-01-01T00:00:00Z")), eq(Instant.parse("2024-01-02T00:00:00Z"))))
                .thenReturn(2L);

        PagedResponse<ExecutionResponse> response = service.listExecutions(
                ORGANIZATION_ID,
                new ExecutionFilter(JOB_ID, ExecutionStatus.CREATED, ExecutionSource.API,
                        Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-02T00:00:00Z"),
                        2, 10)
        );

        assertThat(response.items()).hasSize(2);
        assertThat(response.total()).isEqualTo(2L);
        assertThat(response.page()).isEqualTo(2);
        assertThat(response.size()).isEqualTo(10);
    }

    @Test
    void shouldReturnEmptyPageWhenNoExecutionsExist() {
        authenticate();
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(true);
        when(executionRepository.findByFilter(eq(ORGANIZATION_ID), any(), any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(List.of());
        when(executionRepository.countByFilter(eq(ORGANIZATION_ID), any(), any(), any(), any(), any())).thenReturn(0L);

        PagedResponse<ExecutionResponse> response = service.listExecutions(
                ORGANIZATION_ID, new ExecutionFilter(null, null, null, null, null, 0, 20)
        );

        assertThat(response.items()).isEmpty();
        assertThat(response.total()).isZero();
    }

    @Test
    void shouldCancelExecutionAndActiveAttemptsWhenAllowed() {
        authenticate();
        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "job:execute")).thenReturn(true);
        Execution execution = persistedExecution(Execution.create(ORGANIZATION_ID, JOB_ID, JOB_VERSION_ID, 1, ExecutionSource.MANUAL, null, UUID.randomUUID(), null), 100L);
        ExecutionAttempt first = persistedAttempt(ExecutionAttempt.create(100L, ORGANIZATION_ID, 1, null, null), 200L);
        first.dispatch();
        ExecutionAttempt second = persistedAttempt(ExecutionAttempt.create(100L, ORGANIZATION_ID, 2, null, null), 201L);
        second.start(UUID.randomUUID());
        when(executionRepository.findByExecUuid(execution.getExecUuid())).thenReturn(Optional.of(execution));
        when(attemptRepository.findActiveByExecutionId(100L)).thenReturn(List.of(first, second));
        when(attemptRepository.save(any(ExecutionAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(executionRepository.save(any(Execution.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.cancelExecution(ORGANIZATION_ID, execution.getExecUuid());

        verify(attemptRepository).save(first);
        verify(attemptRepository).save(second);
        verify(executionRepository).save(any(Execution.class));
        verify(auditPort).record("EXECUTION.CANCEL", ORGANIZATION_ID, USER_ID, "executions", 100L, Map.of());
    }

    @Test
    void shouldCancelExecutionEvenWhenNoActiveAttemptsExist() {
        authenticate();
        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "job:execute")).thenReturn(true);
        Execution execution = persistedExecution(Execution.create(ORGANIZATION_ID, JOB_ID, JOB_VERSION_ID, 1, ExecutionSource.API, null, UUID.randomUUID(), null), 100L);
        when(executionRepository.findByExecUuid(execution.getExecUuid())).thenReturn(Optional.of(execution));
        when(attemptRepository.findActiveByExecutionId(100L)).thenReturn(List.of());
        when(executionRepository.save(any(Execution.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.cancelExecution(ORGANIZATION_ID, execution.getExecUuid());

        verify(attemptRepository, never()).save(any(ExecutionAttempt.class));
        verify(executionRepository).save(any(Execution.class));
    }

    @Test
    void shouldThrowWhenCancellingWithoutAuthentication() {
        when(currentActorPort.currentUserId()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelExecution(ORGANIZATION_ID, UUID.randomUUID()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Not authenticated");
    }

    @Test
    void shouldThrowWhenCancellingWithoutPermission() {
        authenticate();
        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "job:execute")).thenReturn(false);

        assertThatThrownBy(() -> service.cancelExecution(ORGANIZATION_ID, UUID.randomUUID()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void shouldThrowWhenCancellingExecutionWithForbiddenStateTransition() {
        authenticate();
        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "job:execute")).thenReturn(true);
        Execution execution = persistedExecution(Execution.create(ORGANIZATION_ID, JOB_ID, JOB_VERSION_ID, 1, ExecutionSource.API, null, UUID.randomUUID(), null), 100L);
        execution.complete(ExecutionStatus.SUCCEEDED);
        when(executionRepository.findByExecUuid(execution.getExecUuid())).thenReturn(Optional.of(execution));

        assertThatThrownBy(() -> service.cancelExecution(ORGANIZATION_ID, execution.getExecUuid()))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void shouldThrowWhenCancellingExecutionThatDoesNotExist() {
        authenticate();
        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "job:execute")).thenReturn(true);
        UUID execUuid = UUID.randomUUID();
        when(executionRepository.findByExecUuid(execUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelExecution(ORGANIZATION_ID, execUuid))
                .isInstanceOf(ExecutionNotFoundException.class);
    }

    private void authenticate() {
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(USER_ID));
    }

    private static Execution persistedExecution(Execution source, Long id) {
        return Execution.reconstitute(
                id,
                source.getExecUuid(),
                source.getOrganizationId(),
                source.getJobId(),
                source.getJobVersionId(),
                source.getPriority(),
                source.getSource(),
                source.getTriggeredAt(),
                source.getScheduledAt(),
                source.getFinalStatus(),
                source.getTotalAttempts(),
                source.getStartedAt(),
                source.getFinishedAt(),
                source.getCorrelationId(),
                source.getExecutionPolicySnapshot(),
                source.getCreatedAt(),
                source.getUpdatedAt()
        );
    }

    private static ExecutionAttempt persistedAttempt(ExecutionAttempt source, Long id) {
        return ExecutionAttempt.reconstitute(
                id,
                source.getAttemptUuid(),
                source.getExecutionId(),
                source.getOrganizationId(),
                source.getAttemptNumber(),
                source.getStatus(),
                source.getStatusReason(),
                source.getErrorClass(),
                source.getScheduledAt(),
                source.getClaimedAt(),
                source.getStartedAt(),
                source.getFinishedAt(),
                source.getDurationMs(),
                source.getExecutorMetadata(),
                source.getIdempotencyKey(),
                source.getOutputSummary(),
                source.getTraceId(),
                source.getCreatedAt(),
                source.getUpdatedAt()
        );
    }
}
