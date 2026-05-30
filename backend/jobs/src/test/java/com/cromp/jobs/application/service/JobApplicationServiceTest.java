package com.cromp.jobs.application.service;

import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.jobs.api.dto.request.*;
import com.cromp.jobs.api.mapper.JobApiMapper;
import com.cromp.jobs.application.port.AuditPort;
import com.cromp.jobs.application.port.ExecutionCreationPort;
import com.cromp.jobs.domain.model.*;
import com.cromp.jobs.domain.model.enums.JobStatus;
import com.cromp.jobs.domain.model.exceptions.InvalidJobStateException;
import com.cromp.jobs.domain.model.exceptions.JobNotFoundException;
import com.cromp.jobs.domain.repository.JobRepositoryPort;
import com.cromp.jobs.domain.repository.JobVersionRepositoryPort;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JobApplicationServiceTest {

    @Mock private JobRepositoryPort jobRepository;
    @Mock private JobVersionRepositoryPort versionRepository;
    @Mock private CurrentActorPort currentActorPort;
    @Mock private PermissionCheckerPort permissionCheckerPort;
    @Mock private AuditPort auditPort;
    @Mock private ExecutionCreationPort executionCreationPort;

    private JobApplicationService service;

    @BeforeEach
    void setUp() {
        service = new JobApplicationService(jobRepository, versionRepository, currentActorPort,
                permissionCheckerPort, auditPort, executionCreationPort, new JobApiMapper());
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(11L));
        when(currentActorPort.currentOrganizationId()).thenReturn(Optional.of(101L));
        when(permissionCheckerPort.isMember(11L, 101L)).thenReturn(true);
        lenient().when(permissionCheckerPort.hasPermission(eq(11L), eq(101L), anyString())).thenReturn(true);
    }

    @Test
    void shouldCreateJobWhenRequestIsValid() {
        CreateJobRequest request = new CreateJobRequest("Daily sync", "  description  ", sampleConfig(), null, 5);
        when(permissionCheckerPort.hasPermission(11L, 101L, "job:create")).thenReturn(true);
        when(jobRepository.existsByNameAndOrganizationId("Daily sync", 101L)).thenReturn(false);
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> savedJob(invocation.getArgument(0)));
        when(versionRepository.save(any(JobVersion.class))).thenAnswer(invocation -> savedVersion(invocation.getArgument(0)));

        var response = service.createJob(request);

        assertThat(response.name()).isEqualTo("Daily sync");
        assertThat(response.description()).isEqualTo("description");
        assertThat(response.currentVersion()).isEqualTo(1);
        verify(jobRepository).save(any(Job.class));
        verify(versionRepository).save(any(JobVersion.class));
        verify(auditPort).record(eq("JOB.CREATE"), eq(101L), eq(11L), eq("jobs"), eq(100L), anyMap());
    }

    @Test
    void shouldThrowWhenCreateDuplicateName() {
        when(permissionCheckerPort.hasPermission(11L, 101L, "job:create")).thenReturn(true);
        when(jobRepository.existsByNameAndOrganizationId("Daily sync", 101L)).thenReturn(true);

        assertThatThrownBy(() -> service.createJob(new CreateJobRequest("Daily sync", null, sampleConfig(), null, 0)))
                .isInstanceOf(InvalidJobStateException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldUpdateJobAndCreateNewVersion() {
        UUID jobUuid = UUID.randomUUID();
        Job existing = Job.reconstitute(100L, jobUuid, 101L, "Daily sync", "old", JobStatus.ACTIVE,
                "default", 1, 11L, Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"), null);
        JobVersion currentVersion = JobVersion.reconstitute(200L, 100L, 1, sampleConfig(), 11L,
                Instant.parse("2024-01-01T00:00:00Z"));
        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.of(existing));
        when(versionRepository.findLatestByJobId(100L)).thenReturn(Optional.of(currentVersion));
        when(versionRepository.findByJobIdOrderByVersionDesc(100L)).thenReturn(List.of(currentVersion));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(versionRepository.save(any(JobVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.updateJob(jobUuid, new UpdateJobRequest("Daily sync v2", "new", sampleConfig(), "queue", 7));

        assertThat(response.name()).isEqualTo("Daily sync v2");
        assertThat(response.description()).isEqualTo("new");
        assertThat(response.queueName()).isEqualTo("queue");
        assertThat(response.priority()).isEqualTo(7);
        assertThat(response.currentVersion()).isEqualTo(2);
        verify(auditPort).record(eq("JOB.UPDATE"), eq(101L), eq(11L), eq("jobs"), eq(100L), anyMap());
    }

    @Test
    void shouldThrowWhenUpdatingForeignOrganizationJob() {
        UUID jobUuid = UUID.randomUUID();
        Job foreign = Job.reconstitute(100L, jobUuid, 999L, "Daily sync", "old", JobStatus.ACTIVE,
                "default", 1, 11L, Instant.now(), Instant.now(), null);
        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.updateJob(jobUuid, new UpdateJobRequest(null, null, null, null, null)))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Foreign organization access denied");
    }

    @Test
    void shouldChangeStatusAndRejectDuplicateTransition() {
        UUID jobUuid = UUID.randomUUID();
        Job existing = Job.reconstitute(100L, jobUuid, 101L, "Daily sync", "old", JobStatus.DISABLED,
                "default", 1, 11L, Instant.now(), Instant.now(), null);
        JobVersion currentVersion = JobVersion.reconstitute(200L, 100L, 1, sampleConfig(), 11L, Instant.now());
        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.of(existing));
        when(versionRepository.findLatestByJobId(100L)).thenReturn(Optional.of(currentVersion));
        when(versionRepository.findByJobIdOrderByVersionDesc(100L)).thenReturn(List.of(currentVersion));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.changeStatus(jobUuid, new ChangeJobStatusRequest(JobStatus.ACTIVE));

        assertThat(response.status()).isEqualTo("ACTIVE");
        verify(auditPort).record(eq("JOB.STATUS_CHANGE"), eq(101L), eq(11L), eq("jobs"), eq(100L), anyMap());

        assertThatThrownBy(() -> service.changeStatus(jobUuid, new ChangeJobStatusRequest(JobStatus.ACTIVE)))
                .isInstanceOf(InvalidJobStateException.class)
                .hasMessage("Job is already ACTIVE");
    }

    @Test
    void shouldReturnJobAndListJobsForCurrentOrganization() {
        UUID jobUuid = UUID.randomUUID();
        Job existing = Job.reconstitute(100L, jobUuid, 101L, "Daily sync", "old", JobStatus.ACTIVE,
                "default", 1, 11L, Instant.now(), Instant.now(), null);
        JobVersion currentVersion = JobVersion.reconstitute(200L, 100L, 1, sampleConfig(), 11L, Instant.now());
        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.of(existing));
        when(versionRepository.findLatestByJobId(100L)).thenReturn(Optional.of(currentVersion));
        when(versionRepository.findByJobIdOrderByVersionDesc(100L)).thenReturn(List.of(currentVersion));
        when(jobRepository.findByOrganizationId(101L)).thenReturn(List.of(existing));
        when(jobRepository.findByOrganizationIdAndStatus(101L, JobStatus.ACTIVE)).thenReturn(List.of(existing));

        assertThat(service.getJob(jobUuid).jobUuid()).isEqualTo(jobUuid);
        assertThat(service.listJobs(null, 20, 0)).hasSize(1);
        assertThat(service.listJobs(JobStatus.ACTIVE, 20, 0)).hasSize(1);
    }

    @Test
    void shouldTriggerJobAndCreateExecutionRecord() {
        UUID jobUuid = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        Job existing = Job.reconstitute(100L, jobUuid, 101L, "Daily sync", "old", JobStatus.ACTIVE,
                "default", 1, 11L, Instant.now(), Instant.now(), null);
        JobVersion currentVersion = JobVersion.reconstitute(200L, 100L, 1, sampleConfig(), 11L, Instant.now());
        when(permissionCheckerPort.hasPermission(11L, 101L, "job:execute")).thenReturn(true);
        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.of(existing));
        when(versionRepository.findLatestByJobId(100L)).thenReturn(Optional.of(currentVersion));
        when(executionCreationPort.createExecution(anyLong(), anyLong(), anyLong(), anyString(), anyLong(),
                anyMap(), eq(correlationId), anyInt(), isNull(), isNull()))
                .thenReturn(UUID.randomUUID());

        var response = service.triggerJob(jobUuid, new TriggerJobRequest(null, correlationId, Map.of("k", "v")));

        assertThat(response.message()).isEqualTo("Execution triggered");
        verify(executionCreationPort).createExecution(eq(101L), eq(100L), eq(200L), eq("MANUAL"), eq(11L),
                eq(Map.of("k", "v")), eq(correlationId), eq(1), isNull(), isNull());
        verify(auditPort).record(eq("JOB.TRIGGER"), eq(101L), eq(11L), eq("jobs"), eq(100L), anyMap());
    }

    @Test
    void shouldDeleteJobAndArchiveIt() {
        UUID jobUuid = UUID.randomUUID();
        Job existing = Job.reconstitute(100L, jobUuid, 101L, "Daily sync", "old", JobStatus.ACTIVE,
                "default", 1, 11L, Instant.now(), Instant.now(), null);
        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.of(existing));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(permissionCheckerPort.hasPermission(11L, 101L, "job:delete")).thenReturn(true);

        service.deleteJob(jobUuid);

        ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(JobStatus.ARCHIVED);
        assertThat(captor.getValue().isDeleted()).isTrue();
        verify(auditPort).record(eq("JOB.ARCHIVE"), eq(101L), eq(11L), eq("jobs"), eq(100L), anyMap());
    }

    private static JobConfig sampleConfig() {
        return new JobConfig(
                JobTarget.forHttp("https://example.com", "POST", Map.of(), null),
                RetryPolicy.defaultPolicy(),
                1_000,
                List.of()
        );
    }

    private static Job savedJob(Job job) {
        return Job.reconstitute(100L, job.getJobUuid(), job.getOrganizationId(), job.getName(), job.getDescription(),
                job.getStatus(), job.getQueueName(), job.getPriority(), job.getCreatedBy(),
                job.getCreatedAt(), job.getUpdatedAt(), job.getDeletedAt());
    }

    private static JobVersion savedVersion(JobVersion version) {
        return JobVersion.reconstitute(200L, version.getJobId(), version.getVersion(), version.getConfig(),
                version.getCreatedBy(), version.getCreatedAt());
    }
}
