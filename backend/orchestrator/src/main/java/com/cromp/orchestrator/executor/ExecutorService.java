package com.cromp.orchestrator.executor;

import com.cromp.common.event.integration.publisher.DomainEventPublisher;
import com.cromp.common.event.domain.job.JobExecutionFailedEvent;
import com.cromp.common.event.domain.job.JobExecutionSucceededEvent;
import com.cromp.executions.application.port.AttemptClaimPort;
import com.cromp.executions.application.port.AttemptCompletionPort;
import com.cromp.executions.api.dto.request.CompleteAttemptRequest;
import com.cromp.executions.api.dto.response.ClaimAttemptResult;
import com.cromp.executions.domain.model.enums.AttemptStatus;
import com.cromp.jobs.domain.model.JobConfig;
import com.cromp.jobs.domain.model.JobSecretRef;
import com.cromp.orchestrator.config.OrchestratorProperties;
import com.cromp.orchestrator.executor.adapter.HttpTaskAdapter;
import com.cromp.orchestrator.executor.adapter.HttpTaskResult;
import com.cromp.orchestrator.executor.secret.SecretResolverService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Executor: периодически забирает PENDING-попытки из БД и выполняет их.
 *
 * <p><b>Алгоритм одного цикла:</b>
 * <ol>
 *   <li>В цикле до {@code batchSize} раз вызывать {@link AttemptClaimPort#claimNextAttempt}.
 *   <li>Каждую захваченную попытку запустить в пуле потоков ({@code orchestratorTaskExecutor}).
 *   <li>В воркере: распарсить jobConfig → разрешить секреты → выполнить HTTP-запрос.
 *   <li>Зафиксировать результат через {@link AttemptCompletionPort}.
 *   <li>При ошибке — завершить попытку со статусом FAILED.
 * </ol>
 *
 * <p><b>Многопоточность:</b> claim выполняется в основном потоке последовательно,
 * исполнение — конкурентно в пуле. Claim использует SELECT FOR UPDATE SKIP LOCKED
 * в БД (через {@code AttemptClaimPortImpl}), поэтому дублей не будет.
 *
 * <p><b>organizationId:</b> поскольку оркестратор работает system-wide, а не в контексте
 * конкретной организации, claim вызывается с {@code organizationId = null}.
 * {@code AttemptClaimPort} должен поддерживать это — выбирать из всех организаций.
 */
@Slf4j
@Service
public class ExecutorService {

    private final AttemptClaimPort claimPort;
    private final AttemptCompletionPort completionPort;
    private final HttpTaskAdapter httpTaskAdapter;
    private final SecretResolverService secretResolverService;
    private final OrchestratorProperties properties;
    private final ObjectMapper objectMapper;
    private final Executor taskExecutor;
    private final DomainEventPublisher domainEventPublisher;

    // Метрики
    private final Counter startedCounter;
    private final Counter successCounter;
    private final Counter failedCounter;
    private final Timer durationTimer;

    public ExecutorService(
            AttemptClaimPort claimPort,
            AttemptCompletionPort completionPort,
            HttpTaskAdapter httpTaskAdapter,
            SecretResolverService secretResolverService,
            OrchestratorProperties properties,
            ObjectMapper objectMapper,
            @Qualifier("orchestratorTaskExecutor") Executor taskExecutor,
            MeterRegistry meterRegistry,
            DomainEventPublisher domainEventPublisher) {

        this.claimPort = claimPort;
        this.completionPort = completionPort;
        this.httpTaskAdapter = httpTaskAdapter;
        this.secretResolverService = secretResolverService;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.taskExecutor = taskExecutor;
        this.domainEventPublisher = domainEventPublisher;

        this.startedCounter = Counter.builder("orchestrator.executor.attempts.started")
                .description("Число захваченных и запущенных попыток")
                .register(meterRegistry);
        this.successCounter = Counter.builder("orchestrator.executor.attempts.success")
                .description("Успешно выполненные попытки")
                .register(meterRegistry);
        this.failedCounter = Counter.builder("orchestrator.executor.attempts.failed")
                .description("Неуспешные попытки (FAILED / TIMEOUT / ошибка)")
                .register(meterRegistry);
        this.durationTimer = Timer.builder("orchestrator.executor.attempt.duration")
                .description("Время выполнения одной попытки")
                .register(meterRegistry);
    }

    // ── Основной цикл ────────────────────────────────────────────────────────

    @Scheduled(fixedDelayString = "${orchestrator.executor.interval-ms:5000}")
    public void run() {
        int batchSize = properties.getExecutor().getBatchSize();
        List<CompletableFuture<Void>> futures = new ArrayList<>(batchSize);

        for (int i = 0; i < batchSize; i++) {
            Optional<ClaimAttemptResult> claimed = tryClaimNext();
            if (claimed.isEmpty()) {
                // Нет больше PENDING попыток — выходим из цикла раньше времени
                break;
            }

            ClaimAttemptResult attempt = claimed.get();
            startedCounter.increment();
            log.info("[executor] claimed attemptUuid={} executionId={}",
                    attempt.attemptUuid(), attempt.executionId());

            // Запускаем каждую попытку в пуле потоков, не блокируя цикл claim
            CompletableFuture<Void> future = CompletableFuture
                    .runAsync(() -> executeAttempt(attempt), taskExecutor);
            futures.add(future);
        }

        // Не ждём завершения воркеров — они завершатся самостоятельно.
        // Следующий цикл начнётся через fixedDelay после возврата из run().
        if (!futures.isEmpty()) {
            log.debug("[executor] dispatched {} attempt(s) to worker pool", futures.size());
        }
    }

    // ── Выполнение одной попытки (в потоке пула) ─────────────────────────────

    private void executeAttempt(ClaimAttemptResult claimed) {
        Timer.Sample sample = Timer.start();

        try {
            // 1. Десериализуем jobConfig из JSON-строки
            JobConfig jobConfig = parseJobConfig(claimed.jobConfig());
            if (jobConfig == null) {
                failAttempt(claimed, "CONFIG_PARSE_ERROR",
                        "Failed to parse jobConfig JSON", "FAILED");
                return;
            }

            // 2. Отмечаем попытку как RUNNING
            completionPort.markRunning(claimed.attemptUuid());

            // 3. Разрешаем секреты батчем
            List<JobSecretRef> secretRefs = jobConfig.secrets();
            Map<String, String> resolvedSecrets = resolveSecrets(claimed, secretRefs);

            // 4. Выполняем HTTP-запрос
            long startMs = System.currentTimeMillis();
            HttpTaskResult result = durationTimer.record(() ->
                    httpTaskAdapter.execute(
                            claimed.attemptUuid(),
                            jobConfig.target(),
                            resolvedSecrets
                    )
            );
            long durationMs = System.currentTimeMillis() - startMs;

            // 5. Фиксируем результат
            if (result.success()) {
                completeAttempt(claimed, result, AttemptStatus.SUCCEEDED);
                successCounter.increment();
                domainEventPublisher.publish(new JobExecutionSucceededEvent(
                        new java.util.UUID(0, 0),
                        "job-" + claimed.jobId(),
                        claimed.attemptUuid(),
                        claimed.organizationId(),
                        durationMs
                ));
            } else {
                handleFailure(claimed, result, jobConfig);
            }

        } catch (Exception e) {
            log.error("[executor] unhandled error in worker attemptUuid={}", claimed.attemptUuid(), e);
            failAttempt(claimed, "INTERNAL_ERROR", e.getMessage(), "FAILED");
            failedCounter.increment();
            domainEventPublisher.publish(new JobExecutionFailedEvent(
                    new java.util.UUID(0, 0),
                    "job-" + claimed.jobId(),
                    claimed.attemptUuid(),
                    claimed.organizationId(),
                    e.getMessage()
            ));
        } finally {
            sample.stop(durationTimer);
        }
    }

    // ── Обработка ошибки и retry ─────────────────────────────────────────────

    /**
     * Определяет: нужен ли retry или фиксируем как FAILED.
     * Классификация ошибки идёт через {@link HttpTaskResult#errorType()}.
     */
    private void handleFailure(ClaimAttemptResult claimed, HttpTaskResult result,
                                JobConfig jobConfig) {
        String errorType = result.errorType();
        boolean isRetryable = jobConfig.retryPolicy().retryableErrors().contains(errorType);

        if (isRetryable) {
            // Помечаем текущую попытку как FAILED — следующая PENDING-попытка
            // будет создана автоматически триггером БД или Janitor'ом,
            // в зависимости от того, как реализован retry в модуле executions.
            // В MVP: просто завершаем с FAILED, retry создаётся на стороне executions.
            log.info("[executor] attempt failed with retryable error={} attemptUuid={}",
                    errorType, claimed.attemptUuid());
        } else {
            log.warn("[executor] attempt failed with non-retryable error={} attemptUuid={}",
                    errorType, claimed.attemptUuid());
        }

        completeAttempt(claimed, result,
                "TIMEOUT".equals(errorType) ? AttemptStatus.TIMEOUT : AttemptStatus.FAILED);
        failedCounter.increment();

        domainEventPublisher.publish(new JobExecutionFailedEvent(
                new java.util.UUID(0, 0),
                "job-" + claimed.jobId(),
                claimed.attemptUuid(),
                claimed.organizationId(),
                result.errorMessage() != null ? result.errorMessage() : errorType
        ));
    }

    // ── Вспомогательные методы ────────────────────────────────────────────────

    private Optional<ClaimAttemptResult> tryClaimNext() {
        try {
            // null = system-wide claim (все организации)
            return claimPort.claimNextAttempt(null);
        } catch (Exception e) {
            log.error("[executor] failed to claim next attempt", e);
            return Optional.empty();
        }
    }

    private JobConfig parseJobConfig(String json) {
        if (json == null || json.isBlank()) {
            log.error("[executor] jobConfig is null or blank");
            return null;
        }
        try {
            return objectMapper.readValue(json, JobConfig.class);
        } catch (Exception e) {
            log.error("[executor] failed to parse jobConfig: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, String> resolveSecrets(ClaimAttemptResult claimed,
                                                List<JobSecretRef> secretRefs) {
        if (secretRefs == null || secretRefs.isEmpty()) {
            return Map.of();
        }
        try {
            return secretResolverService.resolve(claimed.organizationId(), secretRefs);
        } catch (Exception e) {
            log.error("[executor] failed to resolve secrets attemptUuid={}: {}",
                    claimed.attemptUuid(), e.getMessage());
            return Map.of();
        }
    }

    private void completeAttempt(ClaimAttemptResult claimed, HttpTaskResult result,
                                  AttemptStatus status) {
        try {
            String outputSummary = buildOutputSummary(result);
            completionPort.completeAttempt(
                    claimed.attemptUuid(),
                    new CompleteAttemptRequest(
                            status,
                            outputSummary,
                            result.errorClass(),
                            result.errorMessage(),
                            result.rawBody(),
                            result.contentType()
                    )
            );
            log.info("[executor] attempt completed status={} attemptUuid={}",
                    status, claimed.attemptUuid());
        } catch (Exception e) {
            log.error("[executor] failed to complete attempt attemptUuid={}",
                    claimed.attemptUuid(), e);
        }
    }

    private void failAttempt(ClaimAttemptResult claimed, String errorClass,
                              String errorMessage, String statusStr) {
        try {
            completionPort.completeAttempt(
                    claimed.attemptUuid(),
                    new CompleteAttemptRequest(
                            AttemptStatus.FAILED,
                            null,
                            errorClass,
                            errorMessage,
                            null,
                            null
                    )
            );
        } catch (Exception e) {
            log.error("[executor] failed to mark attempt as FAILED attemptUuid={}",
                    claimed.attemptUuid(), e);
        }
    }

    /**
     * Сериализует результат HTTP в компактный JSON-снэпшот для хранения в БД.
     */
    private String buildOutputSummary(HttpTaskResult result) {
        if (result == null) return null;
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "statusCode", result.statusCode(),
                    "success", result.success(),
                    "body", result.body() != null ? result.body() : ""
            ));
        } catch (Exception e) {
            return "{\"error\":\"failed to serialize output\"}";
        }
    }
}