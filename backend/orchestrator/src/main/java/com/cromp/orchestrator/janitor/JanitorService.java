package com.cromp.orchestrator.janitor;

import com.cromp.executions.application.port.StaleAttemptPort;
import com.cromp.executions.domain.model.ExecutionAttempt;
import com.cromp.orchestrator.config.OrchestratorProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Janitor: периодически ищет попытки, зависшие в статусе RUNNING,
 * и помечает их как TIMEOUT.
 *
 * <p>"Зависшая" попытка — та, у которой {@code status = RUNNING}
 * и {@code updated_at < now() - stuckThresholdMinutes}.
 * Это значит воркер, взявший задачу, упал или завис без фиксации результата.
 *
 * <p><b>Алгоритм одного цикла:</b>
 * <ol>
 *   <li>Найти все стухшие попытки через {@link StaleAttemptPort#findStaleAttempts}.
 *   <li>Пометить каждую как TIMEOUT через {@link StaleAttemptPort#markStaleAsTimeout}.
 *   <li>Триггер БД ({@code fn_update_execution_final_status}) автоматически
 *       обновит {@code Execution.finalStatus = FAILED}.
 * </ol>
 *
 * <p>Каждый цикл получает correlationId для трассировки в логах.
 */
@Slf4j
@Service
public class JanitorService {

    private final StaleAttemptPort staleAttemptPort;
    private final OrchestratorProperties properties;

    private final Counter timeoutCounter;
    private final Counter errorCounter;

    public JanitorService(
            StaleAttemptPort staleAttemptPort,
            OrchestratorProperties properties,
            MeterRegistry meterRegistry) {

        this.staleAttemptPort = staleAttemptPort;
        this.properties = properties;

        this.timeoutCounter = Counter.builder("orchestrator.janitor.attempts.timeout")
                .description("Число попыток, помеченных Janitor'ом как TIMEOUT")
                .register(meterRegistry);

        this.errorCounter = Counter.builder("orchestrator.janitor.errors")
                .description("Ошибки в цикле Janitor'а")
                .register(meterRegistry);
    }

    // ── Основной цикл ────────────────────────────────────────────────────────

    @Scheduled(fixedDelayString = "${orchestrator.janitor.interval-ms:60000}")
    public void run() {
        UUID correlationId = UUID.randomUUID();
        int thresholdMinutes = properties.getJanitor().getStuckThresholdMinutes();

        log.debug("[janitor] cycle started thresholdMinutes={} correlationId={}",
                thresholdMinutes, correlationId);

        List<ExecutionAttempt> stale;
        try {
            stale = staleAttemptPort.findStaleAttempts(thresholdMinutes);
        } catch (Exception e) {
            errorCounter.increment();
            log.error("[janitor] failed to query stale attempts correlationId={}", correlationId, e);
            return;
        }

        if (stale.isEmpty()) {
            log.debug("[janitor] no stale attempts found correlationId={}", correlationId);
            return;
        }

        log.warn("[janitor] found {} stale attempt(s), marking as TIMEOUT correlationId={}",
                stale.size(), correlationId);

        for (ExecutionAttempt attempt : stale) {
            log.warn("[janitor] stale attempt attemptUuid={} executionId={} updatedAt={} correlationId={}",
                    attempt.getAttemptUuid(),
                    attempt.getExecutionId(),
                    attempt.getUpdatedAt(),
                    correlationId);
        }

        try {
            staleAttemptPort.markStaleAsTimeout(stale);
            timeoutCounter.increment(stale.size());
            log.info("[janitor] marked {} attempt(s) as TIMEOUT correlationId={}",
                    stale.size(), correlationId);
        } catch (Exception e) {
            errorCounter.increment();
            log.error("[janitor] failed to mark stale attempts as TIMEOUT correlationId={}",
                    correlationId, e);
        }
    }
}