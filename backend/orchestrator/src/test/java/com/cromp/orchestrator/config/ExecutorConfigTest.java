package com.cromp.orchestrator.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Field;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExecutorConfig")
class ExecutorConfigTest {

    @Test
    @DisplayName("should create orchestratorTaskExecutor bean with correct pool size")
    void shouldCreateExecutorWithPoolSize() {
        OrchestratorProperties props = new OrchestratorProperties();
        props.getExecutor().setPoolSize(8);
        ExecutorConfig config = new ExecutorConfig(props);

        Executor executor = config.orchestratorTaskExecutor();

        assertThat(executor).isNotNull();
        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);

        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
        assertThat(taskExecutor.getCorePoolSize()).isEqualTo(8);
        assertThat(taskExecutor.getMaxPoolSize()).isEqualTo(8);
    }

    @Test
    @DisplayName("should have corePoolSize = maxPoolSize = poolSize from properties")
    void shouldHaveEqualCoreAndMaxPool() {
        OrchestratorProperties props = new OrchestratorProperties();
        props.getExecutor().setPoolSize(12);
        ExecutorConfig config = new ExecutorConfig(props);

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.orchestratorTaskExecutor();

        assertThat(executor.getCorePoolSize()).isEqualTo(12);
        assertThat(executor.getMaxPoolSize()).isEqualTo(12);
    }

    @Test
    @DisplayName("should have zero queue capacity")
    void shouldHaveZeroQueueCapacity() {
        OrchestratorProperties props = new OrchestratorProperties();
        ExecutorConfig config = new ExecutorConfig(props);

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.orchestratorTaskExecutor();

        assertThat(executor.getQueueCapacity()).isZero();
    }

    @Test
    @DisplayName("should have thread name prefix orch-worker-")
    void shouldHaveCorrectThreadNamePrefix() {
        OrchestratorProperties props = new OrchestratorProperties();
        ExecutorConfig config = new ExecutorConfig(props);

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.orchestratorTaskExecutor();

        assertThat(executor.getThreadNamePrefix()).isEqualTo("orch-worker-");
    }

    @Test
    @DisplayName("should wait for tasks to complete on shutdown")
    void shouldWaitForTasksOnShutdown() throws Exception {
        OrchestratorProperties props = new OrchestratorProperties();
        ExecutorConfig config = new ExecutorConfig(props);

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.orchestratorTaskExecutor();

        Field waitForTasksField = ThreadPoolTaskExecutor.class.getSuperclass()
                .getDeclaredField("waitForTasksToCompleteOnShutdown");
        waitForTasksField.setAccessible(true);
        assertThat((boolean) waitForTasksField.get(executor)).isTrue();
    }

    @Test
    @DisplayName("should have await termination seconds = 30")
    void shouldHaveAwaitTermination30Seconds() throws Exception {
        OrchestratorProperties props = new OrchestratorProperties();
        ExecutorConfig config = new ExecutorConfig(props);

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.orchestratorTaskExecutor();

        Field awaitTerminationSecondsField = ThreadPoolTaskExecutor.class.getSuperclass()
                .getDeclaredField("awaitTerminationSeconds");
        awaitTerminationSecondsField.setAccessible(true);
        assertThat((int) awaitTerminationSecondsField.get(executor)).isEqualTo(30);
    }

    @Test
    @DisplayName("should use default poolSize 10 from properties")
    void shouldUseDefaultPoolSize() {
        OrchestratorProperties props = new OrchestratorProperties();
        ExecutorConfig config = new ExecutorConfig(props);

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.orchestratorTaskExecutor();

        assertThat(executor.getCorePoolSize()).isEqualTo(10);
    }
}
