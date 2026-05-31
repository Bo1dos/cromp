package com.cromp.analytics.api.mapper;

import com.cromp.analytics.api.dto.AnalyticsDtos;
import com.cromp.analytics.domain.model.AnomalyDetection;
import com.cromp.analytics.domain.model.AnalyticsPeriod;
import com.cromp.analytics.domain.model.ExecutionSummary;
import com.cromp.analytics.domain.model.Prediction;
import com.cromp.analytics.domain.model.enums.AnomalySeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsMapperTest {

    private AnalyticsMapper mapper;

    @BeforeEach
    void setUp() {
        // Use factory to avoid Spring context
        mapper = Mappers.getMapper(AnalyticsMapper.class);
    }

    // ── toSummaryResponse ─────────────────────────────────────────────────

    @Test
    void shouldMapToSummaryResponseCorrectly() {
        Instant now = Instant.now();
        ExecutionSummary summary = new ExecutionSummary(
                1L, null, AnalyticsPeriod.SEVEN_DAYS,
                100, 80, 20, -1.0, 150.0, 200.0, now
        );

        AnalyticsDtos.SummaryResponse response = mapper.toSummaryResponse(summary);

        assertThat(response.organizationId()).isEqualTo(1L);
        assertThat(response.jobId()).isNull();
        assertThat(response.period()).isEqualTo("SEVEN_DAYS");
        assertThat(response.total()).isEqualTo(100);
        assertThat(response.succeeded()).isEqualTo(80);
        assertThat(response.failed()).isEqualTo(20);
        assertThat(response.errorRate()).isCloseTo(0.20, within(0.0001));
        assertThat(response.avgDurationMs()).isEqualTo(150.0);
        assertThat(response.p95DurationMs()).isEqualTo(200.0);
        assertThat(response.calculatedAt()).isEqualTo(now);
    }

    @Test
    void shouldMapPeriodToEnumName() {
        ExecutionSummary summary = new ExecutionSummary(
                1L, null, AnalyticsPeriod.NINETY_DAYS,
                10, 10, 0, -1.0, null, null, Instant.now()
        );

        AnalyticsDtos.SummaryResponse response = mapper.toSummaryResponse(summary);

        assertThat(response.period()).isEqualTo("NINETY_DAYS");
    }

    @Test
    void shouldMapNullDurationsCorrectly() {
        ExecutionSummary summary = new ExecutionSummary(
                1L, null, AnalyticsPeriod.ONE_DAY,
                10, 9, 1, -1.0, null, null, Instant.now()
        );

        AnalyticsDtos.SummaryResponse response = mapper.toSummaryResponse(summary);

        assertThat(response.avgDurationMs()).isNull();
        assertThat(response.p95DurationMs()).isNull();
    }

    @Test
    void shouldMapJobIdCorrectly() {
        ExecutionSummary summary = new ExecutionSummary(
                5L, 42L, AnalyticsPeriod.THIRTY_DAYS,
                50, 45, 5, -1.0, 100.0, 150.0, Instant.now()
        );

        AnalyticsDtos.SummaryResponse response = mapper.toSummaryResponse(summary);

        assertThat(response.jobId()).isEqualTo(42L);
    }

    // ── toPredictionItem ──────────────────────────────────────────────────

    @Test
    void shouldMapToPredictionItemCorrectly() {
        Instant nextRun = Instant.parse("2025-06-15T08:00:00Z");
        Prediction prediction = new Prediction(10L, nextRun, 0.2, 500L, 0.95);

        AnalyticsDtos.PredictionItem item = mapper.toPredictionItem(prediction);

        assertThat(item.jobId()).isEqualTo(10L);
        assertThat(item.nextRunAt()).isEqualTo(nextRun);
        assertThat(item.failureProbability()).isEqualTo(0.2);
        assertThat(item.expectedDurationMs()).isEqualTo(500L);
        assertThat(item.confidence()).isEqualTo(0.95);
    }

    @Test
    void shouldMapPredictionWithNullFields() {
        Prediction prediction = new Prediction(5L, null, 0.0, null, 0.5);

        AnalyticsDtos.PredictionItem item = mapper.toPredictionItem(prediction);

        assertThat(item.nextRunAt()).isNull();
        assertThat(item.expectedDurationMs()).isNull();
    }

    // ── toPredictionsResponse ─────────────────────────────────────────────

    @Test
    void shouldMapToPredictionsResponseCorrectly() {
        Prediction p1 = new Prediction(1L, Instant.now(), 0.1, 100L, 0.9);
        Prediction p2 = new Prediction(2L, Instant.now(), 0.2, 200L, 0.8);
        List<Prediction> predictions = List.of(p1, p2);

        AnalyticsDtos.PredictionsResponse response =
                mapper.toPredictionsResponse(10L, 20L, predictions);

        assertThat(response.organizationId()).isEqualTo(10L);
        assertThat(response.jobId()).isEqualTo(20L);
        assertThat(response.predictions()).hasSize(2);
    }

    @Test
    void shouldHandleEmptyPredictionsList() {
        AnalyticsDtos.PredictionsResponse response =
                mapper.toPredictionsResponse(1L, null, List.of());

        assertThat(response.predictions()).isEmpty();
    }

    @Test
    void shouldHandleNullJobIdInPredictionsResponse() {
        AnalyticsDtos.PredictionsResponse response =
                mapper.toPredictionsResponse(1L, null, List.of());

        assertThat(response.jobId()).isNull();
    }

    // ── toAnomalyItem ─────────────────────────────────────────────────────

    @Test
    void shouldMapToAnomalyItemCorrectly() {
        Instant detectedAt = Instant.parse("2025-04-01T10:00:00Z");
        AnomalyDetection anomaly = new AnomalyDetection(
                3L, "duration_ms", 8000.0, 1000.0, 5000.0,
                AnomalySeverity.HIGH, detectedAt
        );

        AnalyticsDtos.AnomalyItem item = mapper.toAnomalyItem(anomaly);

        assertThat(item.jobId()).isEqualTo(3L);
        assertThat(item.metric()).isEqualTo("duration_ms");
        assertThat(item.value()).isEqualTo(8000.0);
        assertThat(item.expectedMin()).isEqualTo(1000.0);
        assertThat(item.expectedMax()).isEqualTo(5000.0);
        assertThat(item.severity()).isEqualTo("HIGH");
        assertThat(item.detectedAt()).isEqualTo(detectedAt);
    }

    @Test
    void shouldMapSeverityToEnumName() {
        AnomalyDetection anomaly = new AnomalyDetection(
                1L, "error_rate", 0.5, 0.0, 0.1,
                AnomalySeverity.CRITICAL, Instant.now()
        );

        AnalyticsDtos.AnomalyItem item = mapper.toAnomalyItem(anomaly);

        assertThat(item.severity()).isEqualTo("CRITICAL");
    }

    // ── toAnomaliesResponse ───────────────────────────────────────────────

    @Test
    void shouldMapToAnomaliesResponseCorrectly() {
        AnomalyDetection a1 = new AnomalyDetection(
                1L, "duration_ms", 5000.0, 100.0, 2000.0,
                AnomalySeverity.MEDIUM, Instant.now()
        );
        List<AnomalyDetection> anomalies = List.of(a1);

        AnalyticsDtos.AnomaliesResponse response =
                mapper.toAnomaliesResponse(5L, 10L, "2025-01-01", "2025-01-31", anomalies);

        assertThat(response.organizationId()).isEqualTo(5L);
        assertThat(response.jobId()).isEqualTo(10L);
        assertThat(response.from()).isEqualTo("2025-01-01");
        assertThat(response.to()).isEqualTo("2025-01-31");
        assertThat(response.anomalies()).hasSize(1);
    }

    @Test
    void shouldHandleEmptyAnomaliesList() {
        AnalyticsDtos.AnomaliesResponse response =
                mapper.toAnomaliesResponse(1L, null, "2025-01-01", "2025-01-31", List.of());

        assertThat(response.anomalies()).isEmpty();
    }

    private static org.assertj.core.data.Offset<Double> within(double v) {
        return org.assertj.core.data.Offset.offset(v);
    }
}
