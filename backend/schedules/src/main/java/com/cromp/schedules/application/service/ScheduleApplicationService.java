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
import com.cronutils.model.Cron;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

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

    @Override
    public ScheduleResponse createOrUpdate(Long organizationId, Long jobId,
                                           CreateUpdateScheduleRequest request) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        // Проверка прав на управление расписанием
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "job:manage")) {
            throw new SecurityException("No permission to manage schedules");
        }
        // Существует ли задача и принадлежит ли организации
        if (!jobOwnershipPort.jobBelongsToOrganization(jobId, organizationId)) {
            throw new IllegalArgumentException("Job not found or does not belong to organization");
        }

        // Валидация и расчёт следующего запуска
        Cron cron = calculator.validate(request.cronExpression());
        Instant nextRun = calculator.calculateNextRun(cron, request.timezone());

        Optional<Schedule> existing = scheduleRepository.findByJobId(jobId);
        Schedule schedule;
        String action;

        if (existing.isPresent()) {
            schedule = existing.get();
            schedule.updateCron(request.cronExpression(), nextRun);
            schedule.updateTimezone(request.timezone(), nextRun);
            // rules пока не обновляем, оставим на будущее
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
    public ScheduleResponse getSchedule(Long organizationId, Long jobId) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.isMember(userId, organizationId)) {
            throw new SecurityException("Not a member of this organization");
        }
        if (!jobOwnershipPort.jobBelongsToOrganization(jobId, organizationId)) {
            throw new IllegalArgumentException("Job not found or not in organization");
        }
        Schedule schedule = scheduleRepository.findByJobId(jobId)
                .orElseThrow(() -> new ScheduleNotFoundException(jobId));
        return mapper.toResponse(schedule);
    }

    @Override
    public void deleteSchedule(Long organizationId, Long jobId) {
        Long userId = currentActorPort.currentUserId()
                .orElseThrow(() -> new SecurityException("Not authenticated"));
        if (!permissionCheckerPort.hasPermission(userId, organizationId, "job:manage")) {
            throw new SecurityException("No permission to delete schedule");
        }
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