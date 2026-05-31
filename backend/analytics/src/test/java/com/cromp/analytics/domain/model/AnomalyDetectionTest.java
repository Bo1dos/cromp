package com.cromp.analytics.domain.model;

import com.cromp.analytics.domain.model.enums.AnomalySeverity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AnomalyDetectionTest {

    @Test
    void shouldPreserveAllFields() {
        Instant detectedAt = Instant.parse("2025-05-15T12:00:00Z");
        AnomalyDetection anomaly = new AnomalyDetection(
                10L, "duration_ms", 5000.0, 1000.0, 3000.0,
                AnomalySeverity.HIGH, detectedAt
        );

        assertThat(anomaly.jobId()).isEqualTo(10L);
        assertThat(anomaly.metric()).isEqualTo("duration_ms");
        assertThat(anomaly.value()).isEqualTo(5000.0);
        assertThat(anomaly.expectedMin()).isEqualTo(1000.0);
        assertThat(anomaly.expectedMax()).isEqualTo(3000.0);
        assertThat(anomaly.severity()).isEqualTo(AnomalySeverity.HIGH);
        assertThat(anomaly.detectedAt()).isEqualTo(detectedAt);
    }

    @Test
    void shouldSupportAllSeverities() {
        for (AnomalySeverity severity : AnomalySeverity.values()) {
            AnomalyDetection anomaly = new AnomalyDetection(
                    1L, "error_rate", 0.5, 0.0, 0.1,
                    severity, Instant.now()
            );
            assertThat(anomaly.severity()).isEqualTo(severity);
        }
    }

    @Test
    void shouldSupportDifferentMetrics() {
        AnomalyDetection durationAnomaly = new AnomalyDetection(
                1L, "duration_ms", 10000.0, 100.0, 5000.0,
                AnomalySeverity.CRITICAL, Instant.now()
        );
        assertThat(durationAnomaly.metric()).isEqualTo("duration_ms");

        AnomalyDetection errorAnomaly = new AnomalyDetection(
                1L, "error_rate", 0.8, 0.0, 0.2,
                AnomalySeverity.MEDIUM, Instant.now()
        );
        assertThat(errorAnomaly.metric()).isEqualTo("error_rate");
    }
}
