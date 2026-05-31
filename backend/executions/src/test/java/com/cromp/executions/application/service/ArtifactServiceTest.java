package com.cromp.executions.application.service;

import com.cromp.executions.api.dto.request.UploadArtifactRequest;
import com.cromp.executions.api.dto.response.ArtifactResponse;
import com.cromp.executions.api.mapper.ExecutionApiMapper;
import com.cromp.executions.application.port.ArtifactStoragePort;
import com.cromp.executions.application.port.StoredArtifact;
import com.cromp.executions.domain.model.Execution;
import com.cromp.executions.domain.model.ExecutionArtifact;
import com.cromp.executions.domain.model.enums.ArtifactKind;
import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.executions.domain.model.exceptions.ExecutionNotFoundException;
import com.cromp.executions.domain.repository.ExecutionArtifactRepositoryPort;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
class ArtifactServiceTest {

    private static final Long USER_ID = 11L;
    private static final Long ORGANIZATION_ID = 22L;
    private static final Long JOB_ID = 33L;
    private static final Long JOB_VERSION_ID = 44L;

    @Mock private ExecutionRepositoryPort executionRepository;
    @Mock private ExecutionArtifactRepositoryPort artifactRepository;
    @Mock private ArtifactStoragePort storagePort;
    @Mock private CurrentActorPort currentActorPort;
    @Mock private PermissionCheckerPort permissionCheckerPort;

    private ArtifactService service;

    @BeforeEach
    void setUp() {
        service = new ArtifactService(
                executionRepository,
                artifactRepository,
                storagePort,
                new ExecutionApiMapper(),
                currentActorPort,
                permissionCheckerPort
        );
    }

    @Test
    void shouldUploadArtifactUsingFallbackFilenameAndContentTypeWhenMissing() {
        authenticate();
        Execution execution = persistedExecution(Execution.create(ORGANIZATION_ID, JOB_ID, JOB_VERSION_ID, 1, ExecutionSource.MANUAL, null, UUID.randomUUID(), null), 100L);
        byte[] bytes = "hello".getBytes();
        MultipartFile file = new MockMultipartFile("file", null, "application/octet-stream", bytes);
        UploadArtifactRequest request = new UploadArtifactRequest(ArtifactKind.LOG_STDOUT, null, 30);

        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "job:execute")).thenReturn(true);
        when(executionRepository.findByExecUuid(execution.getExecUuid())).thenReturn(Optional.of(execution));
        when(storagePort.store(eq(100L), eq("log_stdout"), eq("application/octet-stream"), any(java.io.InputStream.class), eq((long) bytes.length)))
                .thenReturn(new StoredArtifact("executions/100/log_stdout", bytes.length, "checksum"));
        when(artifactRepository.save(any(ExecutionArtifact.class))).thenAnswer(invocation -> persistedArtifact(invocation.getArgument(0), 200L));

        ArtifactResponse response = service.uploadArtifact(ORGANIZATION_ID, execution.getExecUuid(), request, file);

        ArgumentCaptor<ExecutionArtifact> artifactCaptor = ArgumentCaptor.forClass(ExecutionArtifact.class);
        verify(artifactRepository).save(artifactCaptor.capture());
        assertThat(artifactCaptor.getValue().getKind()).isEqualTo(ArtifactKind.LOG_STDOUT);
        assertThat(artifactCaptor.getValue().getStoragePath()).isEqualTo("executions/100/log_stdout");
        assertThat(artifactCaptor.getValue().getContentType()).isEqualTo("application/octet-stream");
        assertThat(response.id()).isEqualTo(200L);
    }

    @Test
    void shouldUploadArtifactWhenExplicitMetadataIsProvided() {
        authenticate();
        Execution execution = persistedExecution(Execution.create(ORGANIZATION_ID, JOB_ID, JOB_VERSION_ID, 1, ExecutionSource.API, null, UUID.randomUUID(), null), 100L);
        MultipartFile file = new MockMultipartFile("file", "report.json", "application/json", "{\"a\":1}".getBytes());
        UploadArtifactRequest request = new UploadArtifactRequest(ArtifactKind.OUTPUT_PAYLOAD, "application/json", 7);

        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "job:execute")).thenReturn(true);
        when(executionRepository.findByExecUuid(execution.getExecUuid())).thenReturn(Optional.of(execution));
        when(storagePort.store(eq(100L), eq("report.json"), eq("application/json"), any(java.io.InputStream.class), eq(file.getSize())))
                .thenReturn(new StoredArtifact("executions/100/report.json", file.getSize(), "checksum"));
        when(artifactRepository.save(any(ExecutionArtifact.class))).thenAnswer(invocation -> persistedArtifact(invocation.getArgument(0), 200L));

        ArtifactResponse response = service.uploadArtifact(ORGANIZATION_ID, execution.getExecUuid(), request, file);

        assertThat(response.kind()).isEqualTo("OUTPUT_PAYLOAD");
        assertThat(response.contentType()).isEqualTo("application/json");
    }

    @Test
    void shouldThrowWhenUploadingWithoutAuthentication() {
        when(currentActorPort.currentUserId()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.uploadArtifact(ORGANIZATION_ID, UUID.randomUUID(),
                new UploadArtifactRequest(ArtifactKind.LOG_STDOUT, null, null), new MockMultipartFile("file", "a.txt", "text/plain", "a".getBytes())))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Not authenticated");
    }

    @Test
    void shouldThrowWhenUploadingWithoutPermission() {
        authenticate();
        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "job:execute")).thenReturn(false);

        assertThatThrownBy(() -> service.uploadArtifact(ORGANIZATION_ID, UUID.randomUUID(),
                new UploadArtifactRequest(ArtifactKind.LOG_STDOUT, null, null), new MockMultipartFile("file", "a.txt", "text/plain", "a".getBytes())))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void shouldThrowWhenExecutionDoesNotExist() {
        authenticate();
        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "job:execute")).thenReturn(true);
        UUID execUuid = UUID.randomUUID();
        when(executionRepository.findByExecUuid(execUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.uploadArtifact(ORGANIZATION_ID, execUuid,
                new UploadArtifactRequest(ArtifactKind.LOG_STDOUT, null, null), new MockMultipartFile("file", "a.txt", "text/plain", "a".getBytes())))
                .isInstanceOf(ExecutionNotFoundException.class);
    }

    @Test
    void shouldWrapStorageIOExceptionInRuntimeException() {
        authenticate();
        Execution execution = persistedExecution(Execution.create(ORGANIZATION_ID, JOB_ID, JOB_VERSION_ID, 1, ExecutionSource.MANUAL, null, UUID.randomUUID(), null), 100L);
        MultipartFile file = new MockMultipartFile("file", "report.json", "application/json", "{\"a\":1}".getBytes());

        when(permissionCheckerPort.hasPermission(USER_ID, ORGANIZATION_ID, "job:execute")).thenReturn(true);
        when(executionRepository.findByExecUuid(execution.getExecUuid())).thenReturn(Optional.of(execution));
        when(storagePort.store(anyLong(), any(), any(), any(java.io.InputStream.class), anyLong())).thenAnswer(invocation -> { throw new IOException("boom"); });

        assertThatThrownBy(() -> service.uploadArtifact(ORGANIZATION_ID, execution.getExecUuid(),
                new UploadArtifactRequest(ArtifactKind.OUTPUT_PAYLOAD, null, null), file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to upload artifact");
    }

    @Test
    void shouldListArtifactsForOrganizationAndExecution() {
        authenticate();
        Execution execution = persistedExecution(Execution.create(ORGANIZATION_ID, JOB_ID, JOB_VERSION_ID, 1, ExecutionSource.API, null, UUID.randomUUID(), null), 100L);
        ExecutionArtifact artifact = persistedArtifact(ExecutionArtifact.create(100L, ArtifactKind.LOG_STDERR, "path", 1L, "sum", "text/plain", USER_ID), 200L);
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(true);
        when(executionRepository.findByExecUuid(execution.getExecUuid())).thenReturn(Optional.of(execution));
        when(artifactRepository.findByExecutionId(100L)).thenReturn(List.of(artifact));

        List<ArtifactResponse> responses = service.listArtifacts(ORGANIZATION_ID, execution.getExecUuid());

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().storagePath()).isEqualTo("path");
    }

    @Test
    void shouldReturnEmptyArtifactListWhenNoArtifactsExist() {
        authenticate();
        Execution execution = persistedExecution(Execution.create(ORGANIZATION_ID, JOB_ID, JOB_VERSION_ID, 1, ExecutionSource.API, null, UUID.randomUUID(), null), 100L);
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(true);
        when(executionRepository.findByExecUuid(execution.getExecUuid())).thenReturn(Optional.of(execution));
        when(artifactRepository.findByExecutionId(100L)).thenReturn(List.of());

        assertThat(service.listArtifacts(ORGANIZATION_ID, execution.getExecUuid())).isEmpty();
    }

    @Test
    void shouldThrowWhenListingArtifactsWithoutMembership() {
        authenticate();
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.listArtifacts(ORGANIZATION_ID, UUID.randomUUID()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Not a member");
    }

    @Test
    void shouldThrowWhenListingArtifactsWithoutAuthentication() {
        when(currentActorPort.currentUserId()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listArtifacts(ORGANIZATION_ID, UUID.randomUUID()))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Not authenticated");
    }

    @Test
    void shouldThrowWhenListingArtifactsForForeignExecution() {
        authenticate();
        Execution execution = persistedExecution(Execution.create(ORGANIZATION_ID + 1, JOB_ID, JOB_VERSION_ID, 1, ExecutionSource.API, null, UUID.randomUUID(), null), 100L);
        when(permissionCheckerPort.isMember(USER_ID, ORGANIZATION_ID)).thenReturn(true);
        when(executionRepository.findByExecUuid(execution.getExecUuid())).thenReturn(Optional.of(execution));

        assertThatThrownBy(() -> service.listArtifacts(ORGANIZATION_ID, execution.getExecUuid()))
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

    private static ExecutionArtifact persistedArtifact(ExecutionArtifact source, Long id) {
        return ExecutionArtifact.reconstitute(
                id,
                source.getExecutionId(),
                source.getKind(),
                source.getStoragePath(),
                source.getSizeBytes(),
                source.getChecksumSha256(),
                source.getContentType(),
                source.getCompression(),
                source.getMetadata(),
                source.getRetentionDays(),
                source.getUploadedBy(),
                source.getUploadedAt()
        );
    }
}
