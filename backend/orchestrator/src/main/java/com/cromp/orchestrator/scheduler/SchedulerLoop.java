package com.cromp.orchestrator.scheduler;

import com.cromp.executions.application.service.ExecutionApplicationService;
import com.cromp.executions.domain.model.enums.ExecutionSource;
import com.cromp.schedules.domain.service.ScheduleCalculator;
import com.cronutils.model.Cron;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "cromp.orchestrator.scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerLoop {
    private final JdbcTemplate jdbcTemplate;
    private final ExecutionApplicationService executionApplicationService;
    private final ScheduleCalculator scheduleCalculator;

    @Scheduled(fixedDelayString = "${cromp.orchestrator.scheduler.fixed-delay-ms:30000}")
    @Transactional
    public void tick() {
        List<DueSchedule> dueSchedules = findDueSchedules();
        for (DueSchedule due : dueSchedules) {
            try {
                executionApplicationService.createExecutionDomain(
                        due.organizationId(),
                        due.jobId(),
                        due.jobVersionId(),
                        due.priority(),
                        ExecutionSource.SCHEDULED,
                        due.nextRunAt(),
                        null,
                        Map.of("scheduleId", due.scheduleId())
                );
                advanceSchedule(due);
            } catch (RuntimeException ex) {
                log.warn("Failed to schedule job {} for organization {}", due.jobId(), due.organizationId(), ex);
            }
        }
    }

    private List<DueSchedule> findDueSchedules() {
        return jdbcTemplate.query("""
                SELECT s.id AS schedule_id, s.job_id, j.organization_id, j.priority, jv.id AS job_version_id,
                       s.cron_expression, s.timezone, s.next_run_at
                FROM schedules s
                JOIN jobs j ON j.id = s.job_id
                JOIN LATERAL (
                    SELECT id FROM job_versions
                    WHERE job_id = j.id
                    ORDER BY version DESC
                    LIMIT 1
                ) jv ON true
                WHERE s.status = 'ACTIVE'
                  AND j.status = 'ACTIVE'
                  AND j.deleted_at IS NULL
                  AND s.next_run_at IS NOT NULL
                  AND s.next_run_at <= now()
                ORDER BY s.next_run_at
                LIMIT 100
                """, (rs, rowNum) -> new DueSchedule(
                rs.getLong("schedule_id"),
                rs.getLong("job_id"),
                rs.getLong("organization_id"),
                rs.getLong("job_version_id"),
                rs.getInt("priority"),
                rs.getString("cron_expression"),
                rs.getString("timezone"),
                rs.getTimestamp("next_run_at").toInstant()
        ));
    }

    private void advanceSchedule(DueSchedule due) {
        Cron cron = scheduleCalculator.validate(due.cronExpression());
        Instant nextRun = scheduleCalculator.calculateNextRun(cron, due.timezone());
        jdbcTemplate.update("UPDATE schedules SET next_run_at = ?, updated_at = now() WHERE id = ?",
                Timestamp.from(nextRun), due.scheduleId());
    }

    record DueSchedule(Long scheduleId, Long jobId, Long organizationId, Long jobVersionId, int priority,
                       String cronExpression, String timezone, Instant nextRunAt) {}
}
