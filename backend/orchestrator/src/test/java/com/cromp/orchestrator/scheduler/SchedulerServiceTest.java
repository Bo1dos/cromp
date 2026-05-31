package com.cromp.orchestrator.scheduler;

import com.cromp.executions.application.port.ExecutionCreationPort;
import com.cromp.jobs.domain.model.Job;
import com.cromp.jobs.domain.model.JobConfig;
import com.cromp.jobs.domain.model.JobTarget;
import com.cromp.jobs.domain.model.JobVersion;
import com.cromp.jobs.domain.model.RetryPolicy;
import com.cromp.jobs.domain.model.enums.JobStatus;
import com.cromp.jobs.domain.repository.JobRepositoryPort;
import com.cromp.jobs.domain.repository.JobVersionRepositoryPort;
import com.cromp.orchestrator.config.OrchestratorProperties;
import com.cromp.schedules.application.port.ScheduleQueryPort;
import com.cromp.schedules.domain.model.Schedule;
import com.cromp.schedules.domain.repository.ScheduleRepositoryPort;
import com.cromp.schedules.domain.service.ScheduleCalculator;
import com.cronutils.model.Cron;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SchedulerService")
@ExtendWith(MockitoExtension.class)
class SchedulerServiceTest {

    @Mock
    private ScheduleQueryPort scheduleQueryPort;
    @Mock
    private ScheduleRepositoryPort scheduleRepository;
    @Mock
    private JobRepositoryPort jobRepository;
    @Mock
    private JobVersionRepositoryPort jobVersionRepository;
    @Mock
    private ExecutionCreationPort executionCreationPort;
    @Mock
    private ScheduleCalculator scheduleCalculator;

    private OrchestratorProperties properties;
    private MeterRegistry meterRegistry;
    private SchedulerService service;

    @BeforeEach
    void setUp() {
        properties = new OrchestratorProperties();
        meterRegistry = new SimpleMeterRegistry();
        service = new SchedulerService(
                scheduleQueryPort, scheduleRepository, jobRepository,
                jobVersionRepository, executionCreationPort, scheduleCalculator,
                properties, meterRegistry);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Schedule createSchedule(Long jobId) {
        return Schedule.create(jobId, "0 */5 * * *", "UTC", null,
                Instant.now().minusSeconds(60));
    }

    private Job createActiveJob(Long id, Long orgId) {
        return Job.create(UUID.randomUUID(), orgId, "test-job", null,
                "default", 5, 1L);
    }

    private JobVersion createJobVersion(Long jobId) {
        RetryPolicy policy = new RetryPolicy(3, 1000, 2.0,
                List.of("5xx", "TIMEOUT"));
        JobConfig config = new JobConfig(
                JobTarget.forHttp("http://example.com", "POST", null, "{}"),
                policy, 30000, null);
        return JobVersion.create(jobId, 1, config, 1L);
    }

    @Nested
    @DisplayName("run()")
    class Run {

        @Test
        @DisplayName("should query due schedules")
        void shouldQueryDueSchedules() {
            when(scheduleQueryPort.findActiveSchedulesReadyForRun())
                    .thenReturn(List.of());

            service.run();

            verify(scheduleQueryPort).findActiveSchedulesReadyForRun();
        }

        @Test
        @DisplayName("should exit without error when query returns empty")
        void shouldExitWhenEmpty() {
            when(scheduleQueryPort.findActiveSchedulesReadyForRun())
                    .thenReturn(List.of());

            service.run();

            verify(jobRepository, never()).findById(anyLong());
        }

        @Test
        @DisplayName("should exit when query throws exception")
        void shouldExitWhenQueryThrows() {
            when(scheduleQueryPort.findActiveSchedulesReadyForRun())
                    .thenThrow(new RuntimeException("DB unavailable"));

            service.run();

            // Should not propagate exception
            assertThat(meterRegistry.get("orchestrator.scheduler.errors")
                    .counter().count()).isZero();
        }

        @Test
        @DisplayName("should process each due schedule")
        void shouldProcessEachDueSchedule() {
            Schedule s1 = createSchedule(1L);
            Schedule s2 = createSchedule(2L);
            when(scheduleQueryPort.findActiveSchedulesReadyForRun())
                    .thenReturn(List.of(s1, s2));

            // Both jobs not found — skipped
            when(jobRepository.findById(anyLong())).thenReturn(Optional.empty());

            service.run();

            verify(jobRepository, times(2)).findById(anyLong());
        }

        @Test
        @DisplayName("should continue processing when one schedule fails")
        void shouldContinueOnSingleScheduleError() {
            Schedule s1 = createSchedule(1L);
            Schedule s2 = createSchedule(2L);
            when(scheduleQueryPort.findActiveSchedulesReadyForRun())
                    .thenReturn(List.of(s1, s2));

            when(jobRepository.findById(1L))
                    .thenThrow(new RuntimeException("Boom on schedule 1"));
            when(jobRepository.findById(2L)).thenReturn(Optional.empty());

            service.run();

            double errors = meterRegistry.get("orchestrator.scheduler.errors")
                    .counter().count();
            assertThat(errors).isEqualTo(1.0);
            // Second schedule still processed
            verify(jobRepository).findById(2L);
        }
    }

    @Nested
    @DisplayName("processSchedule()")
    class ProcessSchedule {

        @Test
        @DisplayName("should skip when job not found")
        void shouldSkipWhenJobNotFound() {
            Schedule schedule = createSchedule(1L);
            when(jobRepository.findById(1L)).thenReturn(Optional.empty());

            service.processSchedule(schedule, UUID.randomUUID());

            verify(executionCreationPort, never()).createExecution(
                    any(), anyLong(), anyLong(), anyString(),
                    any(), any(), any(), anyInt(), any(), anyString());
            assertThat(meterRegistry.get("orchestrator.scheduler.schedules.skipped")
                    .counter().count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("should skip when job status is not ACTIVE")
        void shouldSkipWhenJobNotActive() {
            Schedule schedule = createSchedule(1L);
            Job job = Job.create(UUID.randomUUID(), 1L, "test", null,
                    "default", 5, 1L);
            job.disable();
            when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

            service.processSchedule(schedule, UUID.randomUUID());

            assertThat(meterRegistry.get("orchestrator.scheduler.schedules.skipped")
                    .counter().count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("should skip when latest job version not found")
        void shouldSkipWhenNoVersion() {
            Schedule schedule = createSchedule(1L);
            Job job = createActiveJob(1L, 1L);
            when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
            when(jobVersionRepository.findLatestByJobId(1L))
                    .thenReturn(Optional.empty());

            service.processSchedule(schedule, UUID.randomUUID());

            assertThat(meterRegistry.get("orchestrator.scheduler.schedules.skipped")
                    .counter().count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("should create execution with correct parameters")
        void shouldCreateExecutionWithCorrectParams() {
            Schedule schedule = createSchedule(1L);
            Job job = createActiveJob(1L, 42L);
            JobVersion version = createJobVersion(1L);

            when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
            when(jobVersionRepository.findLatestByJobId(1L))
                    .thenReturn(Optional.of(version));
            when(executionCreationPort.createExecution(
                    anyLong(), anyLong(), anyLong(), anyString(),
                    any(), any(), any(), anyInt(), any(), anyString()))
                    .thenReturn(UUID.randomUUID());
            when(scheduleCalculator.validate(anyString()))
                    .thenReturn(mockCron());
            when(scheduleCalculator.calculateNextRun(any(Cron.class), anyString()))
                    .thenReturn(Instant.now().plusSeconds(300));

            service.processSchedule(schedule, UUID.randomUUID());

            verify(executionCreationPort).createExecution(
                    eq(42L),           // organizationId
                    eq(1L),            // jobId
                    eq(version.getId()), // jobVersionId
                    eq("SCHEDULED"),    // source
                    isNull(),           // triggeredBy
                    isNull(),           // payload
                    any(),              // correlationId
                    eq(5),              // priority
                    any(),              // scheduledAt
                    anyString()         // snapshot
            );
        }

        @Test
        @DisplayName("should save schedule with advanced nextRunAt")
        void shouldAdvanceAndSaveSchedule() {
            Schedule schedule = createSchedule(1L);
            Job job = createActiveJob(1L, 1L);
            JobVersion version = createJobVersion(1L);

            when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
            when(jobVersionRepository.findLatestByJobId(1L))
                    .thenReturn(Optional.of(version));
            when(executionCreationPort.createExecution(
                    anyLong(), anyLong(), anyLong(), anyString(),
                    any(), any(), any(), anyInt(), any(), anyString()))
                    .thenReturn(UUID.randomUUID());
            when(scheduleCalculator.validate(anyString())).thenReturn(mockCron());
            Instant nextRun = Instant.now().plusSeconds(600);
            when(scheduleCalculator.calculateNextRun(any(Cron.class), anyString()))
                    .thenReturn(nextRun);

            service.processSchedule(schedule, UUID.randomUUID());

            verify(scheduleRepository).save(schedule);
            assertThat(schedule.getNextRunAt()).isEqualTo(nextRun);
        }

        @Test
        @DisplayName("should increment scheduledCounter on success")
        void shouldIncrementScheduledCounter() {
            Schedule schedule = createSchedule(1L);
            Job job = createActiveJob(1L, 1L);
            JobVersion version = createJobVersion(1L);

            when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
            when(jobVersionRepository.findLatestByJobId(1L))
                    .thenReturn(Optional.of(version));
            when(executionCreationPort.createExecution(
                    anyLong(), anyLong(), anyLong(), anyString(),
                    any(), any(), any(), anyInt(), any(), anyString()))
                    .thenReturn(UUID.randomUUID());
            when(scheduleCalculator.validate(anyString())).thenReturn(mockCron());
            when(scheduleCalculator.calculateNextRun(any(Cron.class), anyString()))
                    .thenReturn(Instant.now().plusSeconds(300));

            service.processSchedule(schedule, UUID.randomUUID());

            double count = meterRegistry.get("orchestrator.scheduler.executions.created")
                    .counter().count();
            assertThat(count).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("computeNextRun() fallback")
    class ComputeNextRun {

        @Test
        @DisplayName("should fallback to +24h when validate throws")
        void shouldFallbackWhenValidateThrows() {
            Schedule schedule = createSchedule(1L);
            Job job = createActiveJob(1L, 1L);
            JobVersion version = createJobVersion(1L);

            when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
            when(jobVersionRepository.findLatestByJobId(1L))
                    .thenReturn(Optional.of(version));
            when(executionCreationPort.createExecution(
                    anyLong(), anyLong(), anyLong(), anyString(),
                    any(), any(), any(), anyInt(), any(), anyString()))
                    .thenReturn(UUID.randomUUID());
            when(scheduleCalculator.validate(anyString()))
                    .thenThrow(new RuntimeException("Invalid cron"));

            Instant before = Instant.now();
            service.processSchedule(schedule, UUID.randomUUID());
            Instant after = Instant.now();

            // nextRunAt should be roughly 24h from now
            Instant nextRun = schedule.getNextRunAt();
            assertThat(nextRun).isBetween(
                    before.plusSeconds(86_400).minusSeconds(5),
                    after.plusSeconds(86_400).plusSeconds(5));
        }

        @Test
        @DisplayName("should fallback to +24h when calculateNextRun throws")
        void shouldFallbackWhenCalculateThrows() {
            Schedule schedule = createSchedule(1L);
            Job job = createActiveJob(1L, 1L);
            JobVersion version = createJobVersion(1L);

            when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
            when(jobVersionRepository.findLatestByJobId(1L))
                    .thenReturn(Optional.of(version));
            when(executionCreationPort.createExecution(
                    anyLong(), anyLong(), anyLong(), anyString(),
                    any(), any(), any(), anyInt(), any(), anyString()))
                    .thenReturn(UUID.randomUUID());
            when(scheduleCalculator.validate(anyString())).thenReturn(mockCron());
            when(scheduleCalculator.calculateNextRun(any(Cron.class), anyString()))
                    .thenThrow(new RuntimeException("No next execution"));

            Instant before = Instant.now();
            service.processSchedule(schedule, UUID.randomUUID());
            Instant after = Instant.now();

            Instant nextRun = schedule.getNextRunAt();
            assertThat(nextRun).isBetween(
                    before.plusSeconds(86_400).minusSeconds(5),
                    after.plusSeconds(86_400).plusSeconds(5));
        }
    }

    // ── Mock helper ───────────────────────────────────────────────────────────

    private Cron mockCron() {
        // Use the real ScheduleCalculator to parse a valid expression
        return new ScheduleCalculator().validate("0 */5 * * *");
    }
}
