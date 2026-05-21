package com.cromp.schedules.application.service;

import com.cromp.iam.application.port.CurrentActorPort;
import com.cromp.iam.application.port.PermissionCheckerPort;
import com.cromp.schedules.api.dto.request.CreateUpdateScheduleRequest;
import com.cromp.schedules.api.dto.response.ScheduleResponse;
import com.cromp.schedules.api.mapper.ScheduleApiMapper;
import com.cromp.schedules.api.service.ScheduleFacade;
import com.cromp.schedules.application.port.AuditPort;
import com.cromp.schedules.application.port.JobOwnershipPort;
import com.cromp.schedules.domain.model.Schedule;
import com.cromp.schedules.domain.model.exceptions.ScheduleNotFoundException;
import com.cromp.schedules.domain.repository.ScheduleRepositoryPort;
import com.cromp.schedules.domain.service.ScheduleCalculator;
import com.cromp.jobs.domain.repository.JobRepositoryPort;
import com.cronutils.model.Cron;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleApplicationService implements ScheduleFacade {

    private final ScheduleRepositoryPort scheduleRepository;
    private final ScheduleCalculator calculator;
    private final ScheduleApiMapper mapper;
    private final JobOwnershipPort jobOwnershipPort;
    private final AuditPort auditPort;
    private final CurrentActorPort currentActorPort;
    private final PermissionCheckerPort permissionCheckerPort;
    private final JobRepositoryPort jobRepository;

    @Override
    public ScheduleResponse createOrUpdate(UUID jobUuid, CreateUpdateScheduleRequest request) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "job:manage")) {
            throw new SecurityException("No permission to manage schedules");
        }
        
        Long jobId = jobRepository.findByJobUuid(jobUuid)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"))
                .getId();
        
        if (!jobOwnershipPort.jobBelongsToOrganization(jobId, organizationId)) {
            throw new IllegalArgumentException("Job not found or does not belong to organization");
        }

        Cron cron = calculator.validate(request.cronExpression());
        Instant nextRun = calculator.calculateNextRun(cron, request.timezone());

        Optional<Schedule> existing = scheduleRepository.findByJobId(jobId);
        Schedule schedule;
        String action;

        if (existing.isPresent()) {
            schedule = existing.get();
            schedule.updateCron(request.cronExpression(), nextRun);
            schedule.updateTimezone(request.timezone(), nextRun);
            action = "SCHEDULE.UPDATE";
        } else {
            schedule = Schedule.create(jobId, request.cronExpression(),
                    request.timezone(), request.rules(), nextRun);
            action = "SCHEDULE.CREATE";
        }

        schedule = scheduleRepository.save(schedule);

        auditPort.record(action, organizationId, userId, "schedules",
                schedule.getId(), Map.of("jobId", jobId));

        return mapper.toResponse(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleResponse getSchedule(UUID jobUuid) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
        if (!permissionCheckerPort.isMember(userId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }
        
        Long jobId = jobRepository.findByJobUuid(jobUuid)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"))
                .getId();
        
        if (!jobOwnershipPort.jobBelongsToOrganization(jobId, organizationId)) {
            throw new IllegalArgumentException("Job not found or not in organization");
        }
        Schedule schedule = scheduleRepository.findByJobId(jobId)
                .orElseThrow(() -> new ScheduleNotFoundException(jobId));
        return mapper.toResponse(schedule);
    }

    @Override
    public void deleteSchedule(UUID jobUuid) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        Long organizationId = currentActorPort.currentOrganizationId()
                .orElseThrow(() -> new SecurityException("Not in an organization"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "job:manage")) {
            throw new SecurityException("No permission to delete schedule");
        }
        
        Long jobId = jobRepository.findByJobUuid(jobUuid)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"))
                .getId();
        
        if (!jobOwnershipPort.jobBelongsToOrganization(jobId, organizationId)) {
            throw new IllegalArgumentException("Job not found or not in organization");
        }
        Schedule schedule = scheduleRepository.findByJobId(jobId)
                .orElseThrow(() -> new ScheduleNotFoundException(jobId));
        scheduleRepository.delete(schedule);
        auditPort.record("SCHEDULE.DELETE", organizationId, userId, "schedules",
                schedule.getId(), Map.of("jobId", jobId));
    }
}