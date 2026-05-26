package com.cromp.orchestrator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Конфигурация пула потоков для воркеров Executor'а.
 *
 * Воркеры конкурентно забирают (claim) и выполняют задачи независимо друг от друга.
 * Имя бина — "taskExecutor", чтобы Spring Scheduling не перехватил его как дефолтный.
 */
@Configuration
public class ExecutorConfig {

    private final OrchestratorProperties properties;

    public ExecutorConfig(OrchestratorProperties properties) {
        this.properties = properties;
    }

    @Bean(name = "orchestratorTaskExecutor")
    public Executor orchestratorTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int poolSize = properties.getExecutor().getPoolSize();

        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        // Очередь без буфера — задачи не накапливаются, если все потоки заняты
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("orch-worker-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        return executor;
    }
}