package com.cromp.orchestrator.janitor;

import com.cromp.executions.application.port.StaleAttemptPort;
import com.cromp.executions.domain.model.ExecutionAttempt;
import com.cromp.orchestrator.config.OrchestratorProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("JanitorService")
@ExtendWith(MockitoExtension.class)
class JanitorServiceTest {

    @Mock
    private StaleAttemptPort staleAttemptPort;

    private OrchestratorProperties properties;
    private MeterRegistry meterRegistry;

    private JanitorService service;

    @BeforeEach
    void setUp() {
        properties = new OrchestratorProperties();
        meterRegistry = new SimpleMeterRegistry();
        service = new JanitorService(staleAttemptPort, properties, meterRegistry);
    }

    @Nested
    @DisplayName("run()")
    class Run {

        @Test
        @DisplayName("should use thresholdMinutes from properties")
        void shouldUseThresholdFromProperties() {
            properties.getJanitor().setStuckThresholdMinutes(10);
            when(staleAttemptPort.findStaleAttempts(10)).thenReturn(List.of());

            service.run();

            verify(staleAttemptPort).findStaleAttempts(10);
        }

        @Test
        @DisplayName("should call findStaleAttempts with default threshold")
        void shouldCallFindStaleWithDefaultThreshold() {
            when(staleAttemptPort.findStaleAttempts(5)).thenReturn(List.of());

            service.run();

            verify(staleAttemptPort).findStaleAttempts(5);
        }

        @Test
        @DisplayName("should not call markStaleAsTimeout when no stale attempts")
        void shouldNotMarkWhenNoStaleAttempts() {
            when(staleAttemptPort.findStaleAttempts(anyInt())).thenReturn(List.of());

            service.run();

            verify(staleAttemptPort, never()).markStaleAsTimeout(anyList());
        }

        @Test
        @DisplayName("should mark stale attempts as TIMEOUT when found")
        void shouldMarkStaleAsTimeout() throws Exception {
            ExecutionAttempt attempt = ExecutionAttempt.reconstitute(
                    1L, java.util.UUID.randomUUID(), 100L, 1L, 1,
                    com.cromp.executions.domain.model.enums.AttemptStatus.RUNNING,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null
            );
            List<ExecutionAttempt> stale = List.of(attempt);

            when(staleAttemptPort.findStaleAttempts(anyInt())).thenReturn(stale);

            service.run();

            verify(staleAttemptPort).markStaleAsTimeout(stale);
        }

        @Test
        @DisplayName("should increment timeoutCounter by stale size")
        void shouldIncrementTimeoutCounter() throws Exception {
            ExecutionAttempt a1 = ExecutionAttempt.reconstitute(
                    1L, java.util.UUID.randomUUID(), 100L, 1L, 1,
                    com.cromp.executions.domain.model.enums.AttemptStatus.RUNNING,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null
            );
            ExecutionAttempt a2 = ExecutionAttempt.reconstitute(
                    2L, java.util.UUID.randomUUID(), 101L, 1L, 1,
                    com.cromp.executions.domain.model.enums.AttemptStatus.RUNNING,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null
            );
            when(staleAttemptPort.findStaleAttempts(anyInt())).thenReturn(List.of(a1, a2));

            service.run();

            double count = meterRegistry.get("orchestrator.janitor.attempts.timeout")
                    .counter().count();
            assertThat(count).isEqualTo(2.0);
        }

        @Test
        @DisplayName("should increment errorCounter when findStaleAttempts throws")
        void shouldIncrementErrorCounterOnFindFailure() {
            when(staleAttemptPort.findStaleAttempts(anyInt()))
                    .thenThrow(new RuntimeException("DB error"));

            service.run();

            double count = meterRegistry.get("orchestrator.janitor.errors")
                    .counter().count();
            assertThat(count).isEqualTo(1.0);
        }

        @Test
        @DisplayName("should not call markStaleAsTimeout when findStaleAttempts throws")
        void shouldNotMarkWhenFindThrows() {
            when(staleAttemptPort.findStaleAttempts(anyInt()))
                    .thenThrow(new RuntimeException("DB error"));

            service.run();

            verify(staleAttemptPort, never()).markStaleAsTimeout(anyList());
        }

        @Test
        @DisplayName("should increment errorCounter when markStaleAsTimeout throws")
        void shouldIncrementErrorCounterOnMarkFailure() throws Exception {
            ExecutionAttempt attempt = ExecutionAttempt.reconstitute(
                    1L, java.util.UUID.randomUUID(), 100L, 1L, 1,
                    com.cromp.executions.domain.model.enums.AttemptStatus.RUNNING,
                    null, null, null, null, null, null, null, null, null,
                    null, null, null, null
            );
            when(staleAttemptPort.findStaleAttempts(anyInt())).thenReturn(List.of(attempt));
            doThrow(new RuntimeException("Mark failed")).when(staleAttemptPort).markStaleAsTimeout(anyList());

            service.run();

            double count = meterRegistry.get("orchestrator.janitor.errors")
                    .counter().count();
            assertThat(count).isEqualTo(1.0);
        }

        @Test
        @DisplayName("should handle empty stale list gracefully")
        void shouldHandleEmptyStaleList() {
            when(staleAttemptPort.findStaleAttempts(anyInt())).thenReturn(Collections.emptyList());

            service.run();

            verify(staleAttemptPort, never()).markStaleAsTimeout(anyList());
            double timeoutCount = meterRegistry.get("orchestrator.janitor.attempts.timeout")
                    .counter().count();
            assertThat(timeoutCount).isZero();
        }
    }
}
