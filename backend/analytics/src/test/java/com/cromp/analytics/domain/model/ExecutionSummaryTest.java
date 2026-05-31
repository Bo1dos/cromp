package com.cromp.analytics.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionSummaryTest {

    @Test
    void shouldComputeErrorRateCorrectlyWhenTotalPositive() {
        ExecutionSummary summary = new ExecutionSummary(
                1L, null, AnalyticsPeriod.SEVEN_DAYS,
                100, 80, 20, -1.0, 150.0, 200.0, Instant.now()
        );

        assertThat(summary.errorRate()).isCloseTo(0.20, within(0.0001));
    }

    @Test
    void shouldSetErrorRateToZeroWhenTotalIsZero() {
        ExecutionSummary summary = new ExecutionSummary(
                1L, 42L, AnalyticsPeriod.ONE_DAY,
                0, 0, 0, -1.0, null, null, Instant.now()
        );

        assertThat(summary.total()).isZero();
        assertThat(summary.errorRate()).isZero();
    }

    @Test
    void shouldPreserveAllFields() {
        Instant now = Instant.now();
        ExecutionSummary summary = new ExecutionSummary(
                10L, 20L, AnalyticsPeriod.THIRTY_DAYS,
                50, 45, 5, -1.0, 120.5, 180.0, now
        );

        assertThat(summary.organizationId()).isEqualTo(10L);
        assertThat(summary.jobId()).isEqualTo(20L);
        assertThat(summary.period()).isEqualTo(AnalyticsPeriod.THIRTY_DAYS);
        assertThat(summary.total()).isEqualTo(50);
        assertThat(summary.succeeded()).isEqualTo(45);
        assertThat(summary.failed()).isEqualTo(5);
        assertThat(summary.errorRate()).isCloseTo(0.10, within(0.0001));
        assertThat(summary.avgDurationMs()).isEqualTo(120.5);
        assertThat(summary.p95DurationMs()).isEqualTo(180.0);
        assertThat(summary.calculatedAt()).isEqualTo(now);
    }

    @Test
    void shouldAllowNullDurations() {
        ExecutionSummary summary = new ExecutionSummary(
                1L, null, AnalyticsPeriod.NINETY_DAYS,
                10, 9, 1, -1.0, null, null, Instant.now()
        );

        assertThat(summary.avgDurationMs()).isNull();
        assertThat(summary.p95DurationMs()).isNull();
    }

    @Test
    void shouldAllowNullJobId() {
        ExecutionSummary summary = new ExecutionSummary(
                1L, null, AnalyticsPeriod.SEVEN_DAYS,
                10, 9, 1, -1.0, 100.0, 200.0, Instant.now()
        );

        assertThat(summary.jobId()).isNull();
    }

    @Test
    void shouldComputeErrorRateForAllFailed() {
        ExecutionSummary summary = new ExecutionSummary(
                1L, null, AnalyticsPeriod.SEVEN_DAYS,
                50, 0, 50, -1.0, null, null, Instant.now()
        );

        assertThat(summary.errorRate()).isCloseTo(1.0, within(0.0001));
    }

    @Test
    void shouldComputeErrorRateForAllSucceeded() {
        ExecutionSummary summary = new ExecutionSummary(
                1L, null, AnalyticsPeriod.SEVEN_DAYS,
                50, 50, 0, -1.0, null, null, Instant.now()
        );

        assertThat(summary.errorRate()).isZero();
    }

    private static org.assertj.core.data.Offset<Double> within(double v) {
        return org.assertj.core.data.Offset.offset(v);
    }
}
