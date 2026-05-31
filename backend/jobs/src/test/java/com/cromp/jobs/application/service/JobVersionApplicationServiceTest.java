package com.cromp.jobs.application.service;

import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.jobs.api.mapper.JobVersionApiMapper;
import com.cromp.jobs.application.port.AuditPort;
import com.cromp.jobs.domain.model.*;
import com.cromp.jobs.domain.model.enums.JobStatus;
import com.cromp.jobs.domain.repository.JobRepositoryPort;
import com.cromp.jobs.domain.repository.JobVersionRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JobVersionApplicationServiceTest {

    @Mock private JobVersionRepositoryPort versionRepository;
    @Mock private JobRepositoryPort jobRepository;
    @Mock private CurrentActorPort currentActorPort;
    @Mock private PermissionCheckerPort permissionCheckerPort;
    @Mock private AuditPort auditPort;

    private JobVersionApplicationService service;

    @BeforeEach
    void setUp() {
        service = new JobVersionApplicationService(versionRepository, jobRepository, currentActorPort,
                permissionCheckerPort, auditPort, new JobVersionApiMapper(), new ObjectMapper());
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(11L));
        when(currentActorPort.currentOrganizationId()).thenReturn(Optional.of(101L));
        when(permissionCheckerPort.isMember(11L, 101L)).thenReturn(true);
        when(permissionCheckerPort.hasPermission(11L, 101L, "job:update")).thenReturn(true);
    }

    @Test
    void shouldReturnVersionsForCurrentOrganization() {
        UUID jobUuid = UUID.randomUUID();
        Job job = job(jobUuid, 101L);
        JobVersion version = version(100L, 1);
        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.of(job));
        when(versionRepository.findByJobIdOrderByVersionDesc(100L)).thenReturn(List.of(version));
        when(versionRepository.findByJobIdAndVersion(100L, 1)).thenReturn(Optional.of(version));
        when(versionRepository.findLatestByJobId(100L)).thenReturn(Optional.of(version));

        assertThat(service.getVersions(jobUuid)).hasSize(1);
        assertThat(service.getVersion(jobUuid, 1).version()).isEqualTo(1);
        assertThat(service.getCurrentVersion(jobUuid).version()).isEqualTo(1);
    }

    @Test
    void shouldCompareVersionsAndDetectChanges() {
        UUID jobUuid = UUID.randomUUID();
        Job job = job(jobUuid, 101L);
        JobVersion first = version(100L, 1);
        JobVersion second = JobVersion.reconstitute(2L, 100L, 2, sampleConfig("https://example.com/v2"),
                11L, Instant.parse("2024-01-02T00:00:00Z"));
        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.of(job));
        when(versionRepository.findByJobIdAndVersion(100L, 1)).thenReturn(Optional.of(first));
        when(versionRepository.findByJobIdAndVersion(100L, 2)).thenReturn(Optional.of(second));

        assertThat(service.compareVersions(jobUuid, 1, 2).summary().totalChanges()).isEqualTo(1);
    }

    @Test
    void shouldRevertToVersionAndAudit() {
        UUID jobUuid = UUID.randomUUID();
        Job job = job(jobUuid, 101L);
        JobVersion source = version(100L, 1);
        JobVersion latest = version(100L, 2);
        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.of(job));
        when(versionRepository.findByJobIdAndVersion(100L, 1)).thenReturn(Optional.of(source));
        when(versionRepository.findLatestByJobId(100L)).thenReturn(Optional.of(latest));
        when(versionRepository.save(any(JobVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.revertToVersion(jobUuid, 1);

        assertThat(response.version()).isEqualTo(3);
        verify(auditPort).record(eq("JOB.REVERT"), eq(101L), eq(11L), eq("jobs"), eq(100L), anyMap());
    }

    @Test
    void shouldRejectForeignOrganizationJob() {
        UUID jobUuid = UUID.randomUUID();
        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.of(job(jobUuid, 999L)));

        assertThatThrownBy(() -> service.getVersions(jobUuid))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Foreign organization access denied");
    }

    private static Job job(UUID jobUuid, Long organizationId) {
        return Job.reconstitute(100L, jobUuid, organizationId, "Daily sync", "desc", JobStatus.ACTIVE,
                "default", 1, 11L, Instant.parse("2024-01-01T00:00:00Z"),
                Instant.parse("2024-01-01T00:00:00Z"), null);
    }

    private static JobVersion version(Long jobId, int version) {
        return JobVersion.reconstitute(1L, jobId, version, sampleConfig("https://example.com"), 11L,
                Instant.parse("2024-01-01T00:00:00Z"));
    }

    private static JobConfig sampleConfig(String url) {
        return new JobConfig(
                JobTarget.forHttp(url, "POST", Map.of(), null),
                RetryPolicy.defaultPolicy(),
                1_000,
                List.of()
        );
    }
}
