package com.cromp.orchestrator.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.lang.reflect.Field;

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
    void shouldWaitForTasksOnShutdown() {
        OrchestratorProperties props = new OrchestratorProperties();
        ExecutorConfig config = new ExecutorConfig(props);

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.orchestratorTaskExecutor();

        try {
            Field field = ThreadPoolTaskExecutor.class.getDeclaredField("waitForTasksToCompleteOnShutdown");
            field.setAccessible(true);
            Object value = field.get(executor);
            assertThat(value).isInstanceOf(Boolean.class);
            assertThat((Boolean) value).isTrue();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // If the field is not present, fail the test with the exception
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("should have await termination seconds = 30")
    void shouldHaveAwaitTermination30Seconds() {
        OrchestratorProperties props = new OrchestratorProperties();
        ExecutorConfig config = new ExecutorConfig(props);

        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.orchestratorTaskExecutor();

        try {
            Field field = ThreadPoolTaskExecutor.class.getDeclaredField("awaitTerminationSeconds");
            field.setAccessible(true);
            Object value = field.get(executor);
            assertThat(value).isInstanceOf(Integer.class);
            assertThat((Integer) value).isEqualTo(30);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
