package com.cromp.analytics.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PredictionTest {

    @Test
    void shouldPreserveAllFields() {
        Instant nextRun = Instant.parse("2025-06-01T10:00:00Z");
        Prediction prediction = new Prediction(42L, nextRun, 0.15, 500L, 0.95);

        assertThat(prediction.jobId()).isEqualTo(42L);
        assertThat(prediction.nextRunAt()).isEqualTo(nextRun);
        assertThat(prediction.failureProbability()).isEqualTo(0.15);
        assertThat(prediction.expectedDurationMs()).isEqualTo(500L);
        assertThat(prediction.confidence()).isEqualTo(0.95);
    }

    @Test
    void shouldAllowNullNextRunAt() {
        Prediction prediction = new Prediction(1L, null, 0.0, null, 0.8);

        assertThat(prediction.nextRunAt()).isNull();
        assertThat(prediction.expectedDurationMs()).isNull();
    }

    @Test
    void shouldAllowNullExpectedDurationMs() {
        Prediction prediction = new Prediction(1L, Instant.now(), 0.1, null, 0.9);

        assertThat(prediction.expectedDurationMs()).isNull();
    }

    @Test
    void shouldNotDistortValues() {
        Prediction prediction = new Prediction(
                100L,
                Instant.parse("2025-12-31T23:59:59Z"),
                0.333,
                9999L,
                0.999
        );

        assertThat(prediction.failureProbability()).isEqualTo(0.333);
        assertThat(prediction.expectedDurationMs()).isEqualTo(9999L);
        assertThat(prediction.confidence()).isEqualTo(0.999);
    }

    @Test
    void shouldHandleBoundaryValues() {
        Prediction zeroProb = new Prediction(1L, null, 0.0, null, 0.0);
        assertThat(zeroProb.failureProbability()).isZero();
        assertThat(zeroProb.confidence()).isZero();

        Prediction maxProb = new Prediction(1L, null, 1.0, null, 1.0);
        assertThat(maxProb.failureProbability()).isEqualTo(1.0);
        assertThat(maxProb.confidence()).isEqualTo(1.0);
    }
}
