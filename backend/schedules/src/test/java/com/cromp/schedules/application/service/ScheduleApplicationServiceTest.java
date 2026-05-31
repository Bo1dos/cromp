package com.cromp.schedules.application.service;

import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.jobs.domain.model.Job;
import com.cromp.jobs.domain.model.enums.JobStatus;
import com.cromp.jobs.domain.repository.JobRepositoryPort;
import com.cromp.schedules.api.dto.request.CreateUpdateScheduleRequest;
import com.cromp.schedules.api.dto.response.ScheduleResponse;
import com.cromp.schedules.api.mapper.ScheduleApiMapper;
import com.cromp.schedules.application.port.AuditPort;
import com.cromp.schedules.application.port.JobOwnershipPort;
import com.cromp.schedules.domain.model.Schedule;
import com.cromp.schedules.domain.model.enums.ScheduleStatus;
import com.cromp.schedules.domain.model.exceptions.InvalidCronExpressionException;
import com.cromp.schedules.domain.model.exceptions.ScheduleNotFoundException;
import com.cromp.schedules.domain.repository.ScheduleRepositoryPort;
import com.cromp.schedules.domain.service.ScheduleCalculator;
import com.cronutils.model.Cron;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScheduleApplicationServiceTest {

    @Mock private ScheduleRepositoryPort scheduleRepository;
    @Mock private ScheduleCalculator calculator;
    @Mock private ScheduleApiMapper mapper;
    @Mock private JobOwnershipPort jobOwnershipPort;
    @Mock private AuditPort auditPort;
    @Mock private CurrentActorPort currentActorPort;
    @Mock private PermissionCheckerPort permissionCheckerPort;
    @Mock private JobRepositoryPort jobRepository;

    private ScheduleApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ScheduleApplicationService(scheduleRepository, calculator, mapper,
                jobOwnershipPort, auditPort, currentActorPort, permissionCheckerPort, jobRepository);
        when(currentActorPort.currentUserId()).thenReturn(Optional.of(11L));
        when(currentActorPort.currentOrganizationId()).thenReturn(Optional.of(101L));
        when(permissionCheckerPort.hasPermission(11L, 101L, "job:manage")).thenReturn(true);
        when(permissionCheckerPort.isMember(11L, 101L)).thenReturn(true);
    }

    @Test
    void createOrUpdateShouldCreateNewScheduleWhenNoneExists() {
        UUID jobUuid = UUID.randomUUID();
        long jobId = 77L;
        Cron cron = mock(Cron.class);
        Instant nextRun = Instant.parse("2024-01-01T02:00:00Z");
        CreateUpdateScheduleRequest request = new CreateUpdateScheduleRequest("*/5 * * * *", "Europe/Moscow", "{\"enabled\":true}");
        Schedule persisted = schedule(jobId, request.cronExpression(), request.timezone(), request.rules(), nextRun, ScheduleStatus.ACTIVE);
        ScheduleResponse response = new ScheduleResponse(request.cronExpression(), request.timezone(), request.rules(), nextRun, "ACTIVE",
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T01:00:00Z"));

        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.of(job(jobId, 101L)));
        when(jobOwnershipPort.jobBelongsToOrganization(jobId, 101L)).thenReturn(true);
        when(calculator.validate(request.cronExpression())).thenReturn(cron);
        when(calculator.calculateNextRun(cron, request.timezone())).thenReturn(nextRun);
        when(scheduleRepository.findByJobId(jobId)).thenReturn(Optional.empty());
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(persisted);
        when(mapper.toResponse(persisted)).thenReturn(response);

        ScheduleResponse actual = service.createOrUpdate(jobUuid, request);

        assertThat(actual).isEqualTo(response);
        verify(calculator).validate(request.cronExpression());
        verify(calculator).calculateNextRun(cron, request.timezone());
        verify(scheduleRepository).save(any(Schedule.class));
        verify(auditPort).record(eq("SCHEDULE.CREATE"), eq(101L), eq(11L), eq("schedules"), eq(persisted.getId()), eq(Map.of("jobId", jobId)));
        verify(mapper).toResponse(persisted);
    }

    @Test
    void createOrUpdateShouldUpdateExistingScheduleAndRecordUpdateAction() {
        UUID jobUuid = UUID.randomUUID();
        long jobId = 77L;
        Cron cron = mock(Cron.class);
        Instant nextRun = Instant.parse("2024-01-01T02:00:00Z");
        CreateUpdateScheduleRequest request = new CreateUpdateScheduleRequest("*/5 * * * *", "Europe/Moscow", null);
        Schedule existing = spy(schedule(jobId, "0 */1 * * *", "UTC", null, Instant.parse("2024-01-01T01:00:00Z"), ScheduleStatus.ACTIVE));
        Schedule persisted = schedule(jobId, request.cronExpression(), request.timezone(), null, nextRun, ScheduleStatus.ACTIVE);
        ScheduleResponse response = new ScheduleResponse(request.cronExpression(), request.timezone(), null, nextRun, "ACTIVE",
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T01:00:00Z"));

        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.of(job(jobId, 101L)));
        when(jobOwnershipPort.jobBelongsToOrganization(jobId, 101L)).thenReturn(true);
        when(calculator.validate(request.cronExpression())).thenReturn(cron);
        when(calculator.calculateNextRun(cron, request.timezone())).thenReturn(nextRun);
        when(scheduleRepository.findByJobId(jobId)).thenReturn(Optional.of(existing));
        when(scheduleRepository.save(existing)).thenReturn(persisted);
        when(mapper.toResponse(persisted)).thenReturn(response);

        ScheduleResponse actual = service.createOrUpdate(jobUuid, request);

        assertThat(actual).isEqualTo(response);
        verify(existing).updateCron(request.cronExpression(), nextRun);
        verify(existing).updateTimezone(request.timezone(), nextRun);
        verify(scheduleRepository).save(existing);
        verify(auditPort).record(eq("SCHEDULE.UPDATE"), eq(101L), eq(11L), eq("schedules"), eq(persisted.getId()), eq(Map.of("jobId", jobId)));
    }

    @Test
    void createOrUpdateShouldRejectMissingCurrentUserOrganizationPermissionJobOrCron() {
        UUID jobUuid = UUID.randomUUID();
        CreateUpdateScheduleRequest request = new CreateUpdateScheduleRequest("*/5 * * * *", "Europe/Moscow", null);

        when(currentActorPort.currentUserId()).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createOrUpdate(jobUuid, request))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Not authenticated");

        when(currentActorPort.currentUserId()).thenReturn(Optional.of(11L));
        when(currentActorPort.currentOrganizationId()).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createOrUpdate(jobUuid, request))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Not in an organization");

        when(currentActorPort.currentOrganizationId()).thenReturn(Optional.of(101L));
        when(permissionCheckerPort.hasPermission(11L, 101L, "job:manage")).thenReturn(false);
        assertThatThrownBy(() -> service.createOrUpdate(jobUuid, request))
                .isInstanceOf(SecurityException.class)
                .hasMessage("No permission to manage schedules");

        when(permissionCheckerPort.hasPermission(11L, 101L, "job:manage")).thenReturn(true);
        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createOrUpdate(jobUuid, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Job not found");

        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.of(job(77L, 101L)));
        when(jobOwnershipPort.jobBelongsToOrganization(77L, 101L)).thenReturn(false);
        assertThatThrownBy(() -> service.createOrUpdate(jobUuid, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Job not found or does not belong to organization");

        when(jobOwnershipPort.jobBelongsToOrganization(77L, 101L)).thenReturn(true);
        when(calculator.validate(anyString())).thenThrow(new InvalidCronExpressionException("bad", "invalid"));
        assertThatThrownBy(() -> service.createOrUpdate(jobUuid, request))
                .isInstanceOf(InvalidCronExpressionException.class);
    }

    @Test
    void createOrUpdateShouldPropagateRepositoryErrorsAndSkipAudit() {
        UUID jobUuid = UUID.randomUUID();
        long jobId = 77L;
        Cron cron = mock(Cron.class);
        Instant nextRun = Instant.parse("2024-01-01T02:00:00Z");
        CreateUpdateScheduleRequest request = new CreateUpdateScheduleRequest("*/5 * * * *", "Europe/Moscow", null);

        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.of(job(jobId, 101L)));
        when(jobOwnershipPort.jobBelongsToOrganization(jobId, 101L)).thenReturn(true);
        when(calculator.validate(request.cronExpression())).thenReturn(cron);
        when(calculator.calculateNextRun(cron, request.timezone())).thenReturn(nextRun);
        when(scheduleRepository.findByJobId(jobId)).thenReturn(Optional.empty());
        when(scheduleRepository.save(any(Schedule.class))).thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> service.createOrUpdate(jobUuid, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("db down");
        verifyNoInteractions(auditPort);
    }

    @Test
    void getScheduleShouldReturnResponseWhenUserIsMemberAndJobExists() {
        UUID jobUuid = UUID.randomUUID();
        long jobId = 77L;
        Schedule schedule = schedule(jobId, "*/5 * * * *", "Europe/Moscow", null, Instant.parse("2024-01-01T02:00:00Z"), ScheduleStatus.ACTIVE);
        ScheduleResponse response = new ScheduleResponse("*/5 * * * *", "Europe/Moscow", null,
                Instant.parse("2024-01-01T02:00:00Z"), "ACTIVE",
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T01:00:00Z"));

        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.of(job(jobId, 101L)));
        when(jobOwnershipPort.jobBelongsToOrganization(jobId, 101L)).thenReturn(true);
        when(scheduleRepository.findByJobId(jobId)).thenReturn(Optional.of(schedule));
        when(mapper.toResponse(schedule)).thenReturn(response);

        assertThat(service.getSchedule(jobUuid)).isEqualTo(response);
        verify(permissionCheckerPort).isMember(11L, 101L);
        verify(jobOwnershipPort).jobBelongsToOrganization(jobId, 101L);
    }

    @Test
    void getScheduleShouldRejectInvalidAccessAndMissingSchedule() {
        UUID jobUuid = UUID.randomUUID();
        long jobId = 77L;
        when(currentActorPort.currentUserId()).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getSchedule(jobUuid))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Not authenticated");

        when(currentActorPort.currentUserId()).thenReturn(Optional.of(11L));
        when(currentActorPort.currentOrganizationId()).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getSchedule(jobUuid))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Not in an organization");

        when(currentActorPort.currentOrganizationId()).thenReturn(Optional.of(101L));
        when(permissionCheckerPort.isMember(11L, 101L)).thenReturn(false);
        assertThatThrownBy(() -> service.getSchedule(jobUuid))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Not a member of this organization");

        when(permissionCheckerPort.isMember(11L, 101L)).thenReturn(true);
        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getSchedule(jobUuid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Job not found");

        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.of(job(jobId, 999L)));
        when(jobOwnershipPort.jobBelongsToOrganization(jobId, 101L)).thenReturn(false);
        assertThatThrownBy(() -> service.getSchedule(jobUuid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Job not found or not in organization");

        when(jobOwnershipPort.jobBelongsToOrganization(jobId, 101L)).thenReturn(true);
        when(scheduleRepository.findByJobId(jobId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getSchedule(jobUuid))
                .isInstanceOf(ScheduleNotFoundException.class)
                .hasMessage("Schedule not found for job id=77");
    }

    @Test
    void deleteScheduleShouldDeleteAndAuditWhenAllowed() {
        UUID jobUuid = UUID.randomUUID();
        long jobId = 77L;
        Schedule schedule = schedule(jobId, "*/5 * * * *", "Europe/Moscow", null, Instant.parse("2024-01-01T02:00:00Z"), ScheduleStatus.ACTIVE);

        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.of(job(jobId, 101L)));
        when(jobOwnershipPort.jobBelongsToOrganization(jobId, 101L)).thenReturn(true);
        when(scheduleRepository.findByJobId(jobId)).thenReturn(Optional.of(schedule));

        service.deleteSchedule(jobUuid);

        verify(scheduleRepository).delete(schedule);
        verify(auditPort).record(eq("SCHEDULE.DELETE"), eq(101L), eq(11L), eq("schedules"), eq(schedule.getId()), eq(Map.of("jobId", jobId)));
    }

    @Test
    void deleteScheduleShouldRejectMissingAccessOrSchedule() {
        UUID jobUuid = UUID.randomUUID();
        long jobId = 77L;

        when(permissionCheckerPort.hasPermission(11L, 101L, "job:manage")).thenReturn(false);
        assertThatThrownBy(() -> service.deleteSchedule(jobUuid))
                .isInstanceOf(SecurityException.class)
                .hasMessage("No permission to delete schedule");

        when(permissionCheckerPort.hasPermission(11L, 101L, "job:manage")).thenReturn(true);
        when(currentActorPort.currentUserId()).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteSchedule(jobUuid))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Not authenticated");

        when(currentActorPort.currentUserId()).thenReturn(Optional.of(11L));
        when(currentActorPort.currentOrganizationId()).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteSchedule(jobUuid))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Not in an organization");

        when(currentActorPort.currentOrganizationId()).thenReturn(Optional.of(101L));
        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteSchedule(jobUuid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Job not found");

        when(jobRepository.findByJobUuid(jobUuid)).thenReturn(Optional.of(job(jobId, 999L)));
        when(jobOwnershipPort.jobBelongsToOrganization(jobId, 101L)).thenReturn(false);
        assertThatThrownBy(() -> service.deleteSchedule(jobUuid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Job not found or not in organization");

        when(jobOwnershipPort.jobBelongsToOrganization(jobId, 101L)).thenReturn(true);
        when(scheduleRepository.findByJobId(jobId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteSchedule(jobUuid))
                .isInstanceOf(ScheduleNotFoundException.class);
    }

    private static Job job(Long jobId, Long organizationId) {
        return Job.reconstitute(jobId, UUID.randomUUID(), organizationId, "Daily sync", null, JobStatus.ACTIVE,
                "default", 1, 11L, Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"), null);
    }

    private static Schedule schedule(Long jobId, String cronExpression, String timezone, String rules,
                                     Instant nextRunAt, ScheduleStatus status) {
        return Schedule.reconstitute(100L, jobId, cronExpression, timezone, rules, nextRunAt, status,
                Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T01:00:00Z"), null);
    }
}
