package com.cromp.orchestrator.scheduler;

import com.cromp.executions.application.port.ExecutionCreationPort;
import com.cromp.jobs.domain.model.enums.JobStatus;
import com.cromp.jobs.domain.repository.JobRepositoryPort;
import com.cromp.jobs.domain.repository.JobVersionRepositoryPort;
import com.cromp.orchestrator.config.OrchestratorProperties;
import com.cromp.schedules.application.port.ScheduleQueryPort;
import com.cromp.schedules.domain.model.Schedule;
import com.cromp.schedules.domain.repository.ScheduleRepositoryPort;
import com.cromp.schedules.domain.service.ScheduleCalculator;
import com.cronutils.model.Cron;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Планировщик: периодически проверяет расписания, у которых наступило время запуска,
 * и создаёт для них {@code Execution} с первой попыткой в статусе PENDING.
 *
 * <p><b>Алгоритм одного цикла:</b>
 * <ol>
 *   <li>Запросить у {@link ScheduleQueryPort} все ACTIVE расписания с {@code next_run_at <= now()}.
 *   <li>Для каждого расписания убедиться, что связанная Job — в статусе ACTIVE.
 *   <li>Загрузить актуальную версию Job (latest).
 *   <li>Создать Execution через {@link ExecutionCreationPort}.
 *   <li>Продвинуть {@code next_run_at} расписания на следующий интервал и сохранить.
 * </ol>
 *
 * <p><b>Защита от дублирования:</b> шаги 4–5 выполняются в одной транзакции.
 * Обновление {@code next_run_at} с оптимистичной версией ({@code touch()}) гарантирует,
 * что второй параллельный экземпляр сервиса не создаст дублирующий Execution
 * (он получит {@code OptimisticLockException} при сохранении). Для MVP достаточно.
 *
 * <p>Каждый цикл получает уникальный {@code correlationId} (UUID) для трассировки в логах.
 */
@Slf4j
@Service
public class SchedulerService {

    private final ScheduleQueryPort scheduleQueryPort;
    private final ScheduleRepositoryPort scheduleRepository;
    private final JobRepositoryPort jobRepository;
    private final JobVersionRepositoryPort jobVersionRepository;
    private final ExecutionCreationPort executionCreationPort;
    private final ScheduleCalculator scheduleCalculator;
    private final OrchestratorProperties properties;

    // Метрики
    private final Counter scheduledCounter;
    private final Counter skippedCounter;
    private final Counter errorCounter;

    public SchedulerService(
            ScheduleQueryPort scheduleQueryPort,
            ScheduleRepositoryPort scheduleRepository,
            JobRepositoryPort jobRepository,
            JobVersionRepositoryPort jobVersionRepository,
            ExecutionCreationPort executionCreationPort,
            ScheduleCalculator scheduleCalculator,
            OrchestratorProperties properties,
            MeterRegistry meterRegistry) {

        this.scheduleQueryPort = scheduleQueryPort;
        this.scheduleRepository = scheduleRepository;
        this.jobRepository = jobRepository;
        this.jobVersionRepository = jobVersionRepository;
        this.executionCreationPort = executionCreationPort;
        this.scheduleCalculator = scheduleCalculator;
        this.properties = properties;

        this.scheduledCounter = Counter.builder("orchestrator.scheduler.executions.created")
                .description("Число созданных Execution за все циклы планировщика")
                .register(meterRegistry);

        this.skippedCounter = Counter.builder("orchestrator.scheduler.schedules.skipped")
                .description("Пропущенные расписания (Job не ACTIVE или нет версии)")
                .register(meterRegistry);

        this.errorCounter = Counter.builder("orchestrator.scheduler.errors")
                .description("Ошибки при обработке отдельных расписаний")
                .register(meterRegistry);
    }

    // ── Основной цикл ────────────────────────────────────────────────────────

    @Scheduled(fixedDelayString = "${orchestrator.scheduler.interval-ms:30000}")
    public void run() {
        UUID correlationId = UUID.randomUUID();
        log.debug("[scheduler] cycle started correlationId={}", correlationId);

        List<Schedule> due;
        try {
            due = scheduleQueryPort.findActiveSchedulesReadyForRun();
        } catch (Exception e) {
            log.error("[scheduler] failed to query due schedules correlationId={}", correlationId, e);
            return;
        }

        if (due.isEmpty()) {
            log.debug("[scheduler] no due schedules correlationId={}", correlationId);
            return;
        }

        log.info("[scheduler] found {} due schedule(s) correlationId={}", due.size(), correlationId);

        for (Schedule schedule : due) {
            try {
                processSchedule(schedule, correlationId);
            } catch (Exception e) {
                // Ошибка одного расписания не останавливает обработку остальных
                errorCounter.increment();
                log.error("[scheduler] error processing scheduleId={} jobId={} correlationId={}",
                        schedule.getId(), schedule.getJobId(), correlationId, e);
            }
        }

        log.debug("[scheduler] cycle finished correlationId={}", correlationId);
    }

    // ── Обработка одного расписания ───────────────────────────────────────────

    /**
     * Создаёт Execution и продвигает next_run_at в одной транзакции.
     * Если Job не ACTIVE или у неё нет версии — расписание пропускается.
     */
    @Transactional
    public void processSchedule(Schedule schedule, UUID correlationId) {
        Long jobId = schedule.getJobId();

        // 1. Проверяем, что Job существует и в статусе ACTIVE
        var jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            log.warn("[scheduler] job not found, skipping scheduleId={} jobId={} correlationId={}",
                    schedule.getId(), jobId, correlationId);
            skippedCounter.increment();
            return;
        }

        var job = jobOpt.get();
        if (job.getStatus() != JobStatus.ACTIVE) {
            log.info("[scheduler] job is not ACTIVE (status={}), skipping scheduleId={} jobId={} correlationId={}",
                    job.getStatus(), schedule.getId(), jobId, correlationId);
            skippedCounter.increment();
            return;
        }

        // 2. Загружаем актуальную версию Job
        var versionOpt = jobVersionRepository.findLatestByJobId(jobId);
        if (versionOpt.isEmpty()) {
            log.warn("[scheduler] no job version found, skipping scheduleId={} jobId={} correlationId={}",
                    schedule.getId(), jobId, correlationId);
            skippedCounter.increment();
            return;
        }

        var jobVersion = versionOpt.get();
        Instant now = Instant.now();

        // 3. Создаём Execution — первая попытка автоматически в статусе PENDING
        UUID execCorrelationId = UUID.randomUUID();
        UUID execUuid = executionCreationPort.createExecution(
                job.getOrganizationId(),
                jobId,
                jobVersion.getId(),
                "SCHEDULED",
                null,           // triggeredBy = null, это системный запуск
                null,           // payload пустой для scheduled-запуска
                execCorrelationId,
                job.getPriority(),
                now,
                serializeRetryPolicy(jobVersion)
        );

        log.info("[scheduler] execution created execUuid={} scheduleId={} jobId={} correlationId={}",
                execUuid, schedule.getId(), jobId, correlationId);

        // 4. Продвигаем next_run_at — вычисляем следующее время запуска
        Instant nextRun = computeNextRun(schedule);
        schedule.advanceNextRun(nextRun);
        scheduleRepository.save(schedule);

        log.debug("[scheduler] schedule advanced nextRunAt={} scheduleId={} correlationId={}",
                nextRun, schedule.getId(), correlationId);

        scheduledCounter.increment();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Вычисляет следующее время запуска через {@link ScheduleCalculator}.
     * ScheduleCalculator уже умеет парсить cron и учитывать timezone.
     */
    private Instant computeNextRun(Schedule schedule) {
        try {
            Cron cron = scheduleCalculator.validate(schedule.getCronExpression());
            return scheduleCalculator.calculateNextRun(cron, schedule.getTimezone());
        } catch (Exception e) {
            // Если cron вдруг некорректный — ставим через 24 часа, не падаем
            log.error("[scheduler] failed to compute next run for scheduleId={}, defaulting to +24h",
                    schedule.getId(), e);
            return Instant.now().plusSeconds(86_400);
        }
    }

    /**
     * Сериализует RetryPolicy из JobVersion в строку-снэпшот для Execution.
     * Execution хранит политику повтора на момент создания, чтобы изменение Job
     * не влияло на уже запущенные выполнения.
     */
    private String serializeRetryPolicy(com.cromp.jobs.domain.model.JobVersion version) {
        try {
            var policy = version.getConfig().retryPolicy();
            // Простой JSON-снэпшот без Jackson-зависимости в этом слое
            return String.format(
                    "{\"maxAttempts\":%d,\"backoffMs\":%d,\"backoffMultiplier\":%.1f}",
                    policy.maxAttempts(),
                    policy.backoffMs(),
                    policy.backoffMultiplier()
            );
        } catch (Exception e) {
            log.warn("[scheduler] failed to serialize retry policy for jobVersionId={}, using null",
                    version.getId());
            return null;
        }
    }
}