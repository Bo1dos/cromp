package com.cromp.analytics.infrastructure.mlclient.client;

import com.cromp.analytics.infrastructure.mlclient.contract.MlContracts;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MlContractsTest {

    @Test
    void predictionRequestShouldPreserveAllFields() {
        List<MlContracts.ExecutionRow> history = List.of(
                new MlContracts.ExecutionRow(1L, "2025-01-01", 10, 9, 1, 100.0, 150.0)
        );
        MlContracts.PredictionRequest request = new MlContracts.PredictionRequest(10L, 42L, history);

        assertThat(request.orgId()).isEqualTo(10L);
        assertThat(request.jobId()).isEqualTo(42L);
        assertThat(request.history()).hasSize(1);
    }

    @Test
    void predictionRequestShouldAllowNullJobId() {
        MlContracts.PredictionRequest request = new MlContracts.PredictionRequest(10L, null, List.of());
        assertThat(request.jobId()).isNull();
    }

    @Test
    void executionRowShouldPreserveAllFields() {
        MlContracts.ExecutionRow row = new MlContracts.ExecutionRow(
                5L, "2025-06-01", 100, 95, 5, 200.0, 250.0
        );

        assertThat(row.jobId()).isEqualTo(5L);
        assertThat(row.day()).isEqualTo("2025-06-01");
        assertThat(row.total()).isEqualTo(100);
        assertThat(row.succeeded()).isEqualTo(95);
        assertThat(row.failed()).isEqualTo(5);
        assertThat(row.avgDurationMs()).isEqualTo(200.0);
        assertThat(row.p95DurationMs()).isEqualTo(250.0);
    }

    @Test
    void executionRowShouldAllowNullDurations() {
        MlContracts.ExecutionRow row = new MlContracts.ExecutionRow(
                1L, "2025-01-01", 10, 10, 0, null, null
        );

        assertThat(row.avgDurationMs()).isNull();
        assertThat(row.p95DurationMs()).isNull();
    }

    @Test
    void predictionResponseShouldPreservePredictions() {
        MlContracts.PredictionItem item = new MlContracts.PredictionItem(
                1L, "2025-06-15T08:00:00Z", 0.15, 500L, 0.95
        );
        MlContracts.PredictionResponse response = new MlContracts.PredictionResponse(List.of(item));

        assertThat(response.predictions()).hasSize(1);
        assertThat(response.predictions().get(0).jobId()).isEqualTo(1L);
    }

    @Test
    void predictionItemShouldPreserveAllFields() {
        MlContracts.PredictionItem item = new MlContracts.PredictionItem(
                10L, "2025-07-01T12:00:00Z", 0.25, null, 0.80
        );

        assertThat(item.jobId()).isEqualTo(10L);
        assertThat(item.nextRunAt()).isEqualTo("2025-07-01T12:00:00Z");
        assertThat(item.failureProbability()).isEqualTo(0.25);
        assertThat(item.expectedDurationMs()).isNull();
        assertThat(item.confidence()).isEqualTo(0.80);
    }

    @Test
    void anomalyRequestShouldPreserveAllFields() {
        List<MlContracts.ExecutionRow> history = List.of();
        MlContracts.AnomalyRequest request = new MlContracts.AnomalyRequest(
                1L, 2L, "2025-01-01", "2025-01-31", history
        );

        assertThat(request.orgId()).isEqualTo(1L);
        assertThat(request.jobId()).isEqualTo(2L);
        assertThat(request.from()).isEqualTo("2025-01-01");
        assertThat(request.to()).isEqualTo("2025-01-31");
        assertThat(request.history()).isEmpty();
    }

    @Test
    void anomalyResponseShouldPreserveAnomalies() {
        MlContracts.AnomalyItem item = new MlContracts.AnomalyItem(
                1L, "duration_ms", 5000.0, 1000.0, 3000.0, "HIGH", "2025-05-15T10:00:00Z"
        );
        MlContracts.AnomalyResponse response = new MlContracts.AnomalyResponse(List.of(item));

        assertThat(response.anomalies()).hasSize(1);
    }

    @Test
    void anomalyItemShouldPreserveAllFields() {
        MlContracts.AnomalyItem item = new MlContracts.AnomalyItem(
                5L, "error_rate", 0.75, 0.0, 0.2, "CRITICAL", "2025-06-01T00:00:00Z"
        );

        assertThat(item.jobId()).isEqualTo(5L);
        assertThat(item.metric()).isEqualTo("error_rate");
        assertThat(item.value()).isEqualTo(0.75);
        assertThat(item.expectedMin()).isZero();
        assertThat(item.expectedMax()).isEqualTo(0.2);
        assertThat(item.severity()).isEqualTo("CRITICAL");
        assertThat(item.detectedAt()).isEqualTo("2025-06-01T00:00:00Z");
    }

    @Test
    void emptyPredictionResponseShouldHaveEmptyList() {
        MlContracts.PredictionResponse response = new MlContracts.PredictionResponse(List.of());
        assertThat(response.predictions()).isEmpty();
    }

    @Test
    void emptyAnomalyResponseShouldHaveEmptyList() {
        MlContracts.AnomalyResponse response = new MlContracts.AnomalyResponse(List.of());
        assertThat(response.anomalies()).isEmpty();
    }
}
