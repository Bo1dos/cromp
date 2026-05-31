package com.cromp.orchestrator.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrchestratorProperties")
class OrchestratorPropertiesTest {

    private final OrchestratorProperties properties = new OrchestratorProperties();

    @Nested
    @DisplayName("default values")
    class DefaultValues {

        @Test
        @DisplayName("should have scheduler.intervalMs = 30000")
        void shouldHaveDefaultSchedulerInterval() {
            assertThat(properties.getScheduler().getIntervalMs()).isEqualTo(30_000);
        }

        @Test
        @DisplayName("should have executor.intervalMs = 5000")
        void shouldHaveDefaultExecutorInterval() {
            assertThat(properties.getExecutor().getIntervalMs()).isEqualTo(5_000);
        }

        @Test
        @DisplayName("should have executor.poolSize = 10")
        void shouldHaveDefaultPoolSize() {
            assertThat(properties.getExecutor().getPoolSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("should have executor.batchSize = 20")
        void shouldHaveDefaultBatchSize() {
            assertThat(properties.getExecutor().getBatchSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("should have executor.httpTimeoutMs = 30000")
        void shouldHaveDefaultHttpTimeout() {
            assertThat(properties.getExecutor().getHttpTimeoutMs()).isEqualTo(30_000);
        }

        @Test
        @DisplayName("should have janitor.intervalMs = 60000")
        void shouldHaveDefaultJanitorInterval() {
            assertThat(properties.getJanitor().getIntervalMs()).isEqualTo(60_000);
        }

        @Test
        @DisplayName("should have janitor.stuckThresholdMinutes = 5")
        void shouldHaveDefaultStuckThreshold() {
            assertThat(properties.getJanitor().getStuckThresholdMinutes()).isEqualTo(5);
        }

        @Test
        @DisplayName("should have dryRun = false")
        void shouldHaveDefaultDryRun() {
            assertThat(properties.isDryRun()).isFalse();
        }
    }

    @Nested
    @DisplayName("setters")
    class Setters {

        @Test
        @DisplayName("should allow setting scheduler interval")
        void shouldSetSchedulerInterval() {
            properties.getScheduler().setIntervalMs(10_000);
            assertThat(properties.getScheduler().getIntervalMs()).isEqualTo(10_000);
        }

        @Test
        @DisplayName("should allow setting executor pool size")
        void shouldSetExecutorPoolSize() {
            properties.getExecutor().setPoolSize(20);
            assertThat(properties.getExecutor().getPoolSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("should allow setting executor batch size")
        void shouldSetExecutorBatchSize() {
            properties.getExecutor().setBatchSize(50);
            assertThat(properties.getExecutor().getBatchSize()).isEqualTo(50);
        }

        @Test
        @DisplayName("should allow setting janitor threshold")
        void shouldSetJanitorThreshold() {
            properties.getJanitor().setStuckThresholdMinutes(10);
            assertThat(properties.getJanitor().getStuckThresholdMinutes()).isEqualTo(10);
        }

        @Test
        @DisplayName("should allow setting dryRun")
        void shouldSetDryRun() {
            properties.setDryRun(true);
            assertThat(properties.isDryRun()).isTrue();
        }
    }
}
