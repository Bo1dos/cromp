package com.cromp.executions.application.service;

import com.cromp.executions.api.dto.request.CompleteAttemptRequest;
import com.cromp.executions.api.dto.response.AttemptResponse;
import com.cromp.executions.api.dto.response.ClaimAttemptResult;
import com.cromp.executions.api.mapper.ExecutionApiMapper;
import com.cromp.executions.domain.model.ExecutionAttempt;
import com.cromp.executions.domain.model.enums.AttemptStatus;
import com.cromp.executions.domain.model.exceptions.AttemptNotFoundException;
import com.cromp.executions.domain.model.exceptions.InvalidStatusTransitionException;
import com.cromp.executions.domain.repository.ExecutionAttemptRepositoryPort;
import com.cromp.executions.infrastructure.persistence.custom.ExecutionAttemptCustomRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AttemptServiceTest {

    private static final Long USER_ID = 11L;
    private static final Long ORGANIZATION_ID = 22L;

    @Mock private ExecutionAttemptRepositoryPort attemptRepository;
    @Mock private ExecutionAttemptCustomRepository customRepository;
    @Mock private CurrentActorPort currentActorPort;
    @Mock private PermissionCheckerPort permissionCheckerPort;

    private AttemptService service;

    @BeforeEach
    void setUp() {
        service = new AttemptService(
                attemptRepository,
                customRepository,
                new ExecutionApiMapper(),
                currentActorPort,
                permissionCheckerPort
        );
    }

    @Test
    void shouldReturnClaimedAttemptWhenCustomRepositoryReturnsResult() {
        ClaimAttemptResult result = new ClaimAttemptResult(1L, UUID.randomUUID(), 10L, "{\"job\":true}");
        when(customRepository.claimAttempt(ORGANIZATION_ID)).thenReturn(Optional.of(result));

        assertThat(service.claimNextAttempt(ORGANIZATION_ID)).contains(result);
    }

    @Test
    void shouldReturnEmptyClaimWhenNoAttemptCanBeClaimed() {
        when(customRepository.claimAttempt(ORGANIZATION_ID)).thenReturn(Optional.empty());

        assertThat(service.claimNextAttempt(ORGANIZATION_ID)).isEmpty();
    }

    @Test
    void shouldCompleteAttemptWhenTerminalStatusIsAllowed() {
        ExecutionAttempt attempt = persistedAttempt(ExecutionAttempt.create(10L, ORGANIZATION_ID, 1, null, null), 100L);
        attempt.start(UUID.randomUUID());
        when(attemptRepository.findByAttemptUuid(attempt.getAttemptUuid())).thenReturn(Optional.of(attempt));
        when(attemptRepository.save(any(ExecutionAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.completeAttempt(attempt.getAttemptUuid(), new CompleteAttemptRequest(
                AttemptStatus.SUCCEEDED, "{\"ok\":true}", null, "done"
        ));

        ArgumentCaptor<ExecutionAttempt> captor = ArgumentCaptor.forClass(ExecutionAttempt.class);
        verify(attemptRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AttemptStatus.SUCCEEDED);
        assertThat(captor.getValue().getOutputSummary()).isEqualTo("{\"ok\":true}");
        assertThat(captor.getValue().getStatusReason()).isEqualTo("done");
        assertThat(captor.getValue().getDurationMs()).isNotNull();
    }

    @Test
    void shouldCompleteAttemptForAllTerminalStatuses() {
        for (AttemptStatus terminal : List.of(AttemptStatus.SUCCEEDED, AttemptStatus.FAILED, AttemptStatus.TIMEOUT, AttemptStatus.CANCELLED)) {
            ExecutionAttempt attempt = persistedAttempt(ExecutionAttempt.create(10L, ORGANIZATION_ID, 1, null, null), 100L);
            attempt.start(UUID.randomUUID());
            when(attemptRepository.findByAttemptUuid(attempt.getAttemptUuid())).thenReturn(Optional.of(attempt));
            when(attemptRepository.save(any(ExecutionAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

            service.completeAttempt(attempt.getAttemptUuid(), new CompleteAttemptRequest(terminal, "out", "err", "reason"));

            verify(attemptRepository).save(any(ExecutionAttempt.class));
            org.mockito.Mockito.reset(attemptRepository);
        }
    }

    @Test
    void shouldThrowWhenCompletingWithNonTerminalStatus() {
        assertThatThrownBy(() -> service.completeAttempt(UUID.randomUUID(), new CompleteAttemptRequest(AttemptStatus.RUNNING, null, null, null)))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("terminal statuses");
    }

    @Test
    void shouldThrowWhenAttemptDoesNotExist() {
        UUID attemptUuid = UUID.randomUUID();
        when(attemptRepository.findByAttemptUuid(attemptUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.completeAttempt(attemptUuid, new CompleteAttemptRequest(AttemptStatus.SUCCEEDED, null, null, null)))
                .isInstanceOf(AttemptNotFoundException.class);
    }

    @Test
    void shouldThrowWhenAttemptStateTransitionIsForbidden() {
        ExecutionAttempt attempt = persistedAttempt(ExecutionAttempt.create(10L, ORGANIZATION_ID, 1, null, null), 100L);
        when(attemptRepository.findByAttemptUuid(attempt.getAttemptUuid())).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> service.completeAttempt(attempt.getAttemptUuid(), new CompleteAttemptRequest(AttemptStatus.SUCCEEDED, null, null, null)))
                .isInstanceOf(InvalidStatusTransitionException.class);
        verify(attemptRepository, never()).save(any());
    }

    @Test
    void shouldListAttemptsWhenMemberAndExecutionBelongsToOrganization() {
        authenticate();
        ExecutionAttempt attempt = persistedAttempt(ExecutionAttempt.create(10L, ORGANIZATION_ID, 1, null, null), 100L);
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(true);

        UUID execUuid = UUID.randomUUID();
        when(customRepository.findAttemptsByExecUuid(ORGANIZATION_ID, execUuid)).thenReturn(List.of(attempt));

        List<AttemptResponse> responses = service.listAttempts(ORGANIZATION_ID, execUuid);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().attemptUuid()).isEqualTo(attempt.getAttemptUuid());
    }

    @Test
    void shouldThrowWhenListingAttemptsWithoutAuthentication() {
        when(currentActorPort.currentUserId()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listAttempts(ORGANIZATION_ID, UUID.randomUUID()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Not authenticated");
    }

    @Test
    void shouldThrowWhenListingAttemptsWithoutMembership() {
        authenticate();
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.listAttempts(ORGANIZATION_ID, UUID.randomUUID()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Not a member");
    }

    @Test
    void shouldFindStaleAttemptsUsingCalculatedCutoff() {
        service.findStaleAttempts(15);

        ArgumentCaptor<Instant> cutoffCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(attemptRepository).findByStatusAndUpdatedAtBefore(eq(AttemptStatus.RUNNING), cutoffCaptor.capture());
        assertThat(cutoffCaptor.getValue()).isBefore(Instant.now().minusSeconds(14 * 60L));
    }

    @Test
    void shouldMarkStaleAttemptsAsTimeout() {
        ExecutionAttempt attempt = persistedAttempt(ExecutionAttempt.create(10L, ORGANIZATION_ID, 1, null, null), 100L);
        attempt.start(UUID.randomUUID());
        when(attemptRepository.save(any(ExecutionAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.markStaleAsTimeout(List.of(attempt));

        ArgumentCaptor<ExecutionAttempt> captor = ArgumentCaptor.forClass(ExecutionAttempt.class);
        verify(attemptRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AttemptStatus.TIMEOUT);
        assertThat(captor.getValue().getStatusReason()).isEqualTo("Timed out by janitor");
    }

    @Test
    void shouldThrowWhenMarkingStaleAttemptWithForbiddenTransition() {
        ExecutionAttempt attempt = persistedAttempt(ExecutionAttempt.create(10L, ORGANIZATION_ID, 1, null, null), 100L);
        attempt.complete(AttemptStatus.SUCCEEDED, null, null, null);

        assertThatThrownBy(() -> service.markStaleAsTimeout(List.of(attempt)))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    private void authenticate() {
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(USER_ID));
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
