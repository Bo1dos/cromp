package com.cromp.orchestrator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Централизованные настройки оркестратора.
 * Все значения читаются из application.yml под префиксом "orchestrator".
 */
@Component
@ConfigurationProperties(prefix = "orchestrator")
public class OrchestratorProperties {

    private final Scheduler scheduler = new Scheduler();
    private final Executor executor = new Executor();
    private final Janitor janitor = new Janitor();
    private boolean dryRun = false;

    // ── Scheduler ────────────────────────────────────────────────────────────

    public static class Scheduler {
        /** Интервал между циклами планировщика (мс). */
        private long intervalMs = 30_000;

        public long getIntervalMs() { return intervalMs; }
        public void setIntervalMs(long intervalMs) { this.intervalMs = intervalMs; }
    }

    // ── Executor ─────────────────────────────────────────────────────────────

    public static class Executor {
        /** Интервал между циклами забора задач (мс). */
        private long intervalMs = 5_000;
        /** Количество потоков в пуле. */
        private int poolSize = 10;
        /** Максимальное число задач за один цикл. */
        private int batchSize = 20;
        /** Таймаут одного HTTP-запроса (мс). */
        private long httpTimeoutMs = 30_000;

        public long getIntervalMs() { return intervalMs; }
        public void setIntervalMs(long intervalMs) { this.intervalMs = intervalMs; }

        public int getPoolSize() { return poolSize; }
        public void setPoolSize(int poolSize) { this.poolSize = poolSize; }

        public int getBatchSize() { return batchSize; }
        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }

        public long getHttpTimeoutMs() { return httpTimeoutMs; }
        public void setHttpTimeoutMs(long httpTimeoutMs) { this.httpTimeoutMs = httpTimeoutMs; }
    }

    // ── Janitor ──────────────────────────────────────────────────────────────

    public static class Janitor {
        /** Интервал между циклами уборщика (мс). */
        private long intervalMs = 60_000;
        /** Порог "зависшей" попытки: старше N минут в статусе RUNNING. */
        private int stuckThresholdMinutes = 5;

        public long getIntervalMs() { return intervalMs; }
        public void setIntervalMs(long intervalMs) { this.intervalMs = intervalMs; }

        public int getStuckThresholdMinutes() { return stuckThresholdMinutes; }
        public void setStuckThresholdMinutes(int stuckThresholdMinutes) {
            this.stuckThresholdMinutes = stuckThresholdMinutes;
        }
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public Scheduler getScheduler() { return scheduler; }
    public Executor getExecutor() { return executor; }
    public Janitor getJanitor() { return janitor; }

    /**
     * Dry-run режим: все шаги выполняются, но HTTP-запросы не отправляются.
     * Полезен для отладки и тестирования.
     */
    public boolean isDryRun() { return dryRun; }
    public void setDryRun(boolean dryRun) { this.dryRun = dryRun; }
}