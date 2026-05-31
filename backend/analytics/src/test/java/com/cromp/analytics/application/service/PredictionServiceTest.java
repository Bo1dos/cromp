package com.cromp.analytics.application.service;

import com.cromp.analytics.domain.model.AnomalyDetection;
import com.cromp.analytics.domain.model.Prediction;
import com.cromp.analytics.domain.model.enums.AnomalySeverity;
import com.cromp.analytics.domain.repository.AnalyticsRepository;
import com.cromp.analytics.domain.repository.AnalyticsRepository.DailyExecutionRow;
import com.cromp.analytics.infrastructure.mlclient.client.MlClient;
import com.cromp.analytics.infrastructure.mlclient.contract.MlContracts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    @Mock
    private MlClient mlClient;

    @InjectMocks
    private PredictionService predictionService;

    // ── getPredictions ────────────────────────────────────────────────────

    @Test
    void shouldReturnEmptyPredictionsWhenHistoryIsEmpty() {
        when(analyticsRepository.findDailyRows(1L, 42L, 90)).thenReturn(List.of());

        List<Prediction> result = predictionService.getPredictions(1L, 42L);

        assertThat(result).isEmpty();
        verify(mlClient, never()).getPredictions(any());
    }

    @Test
    void shouldReturnEmptyPredictionsWhenHistoryIsEmptyForOrganization() {
        when(analyticsRepository.findDailyRows(1L, null, 90)).thenReturn(List.of());

        List<Prediction> result = predictionService.getPredictions(1L, null);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldCallMlClientWithCorrectRequestWhenHistoryExists() {
        DailyExecutionRow row = new DailyExecutionRow(42L, "2025-06-01", 10, 9, 1, 100.0, 150.0);
        when(analyticsRepository.findDailyRows(1L, 42L, 90)).thenReturn(List.of(row));

        MlContracts.PredictionItem mlItem = new MlContracts.PredictionItem(
                42L, "2025-06-15T08:00:00Z", 0.2, 500L, 0.95
        );
        MlContracts.PredictionResponse mlResponse =
                new MlContracts.PredictionResponse(List.of(mlItem));
        when(mlClient.getPredictions(any())).thenReturn(mlResponse);

        List<Prediction> result = predictionService.getPredictions(1L, 42L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).jobId()).isEqualTo(42L);
        assertThat(result.get(0).failureProbability()).isEqualTo(0.2);
        assertThat(result.get(0).confidence()).isEqualTo(0.95);
        assertThat(result.get(0).expectedDurationMs()).isEqualTo(500L);
    }

    @Test
    void shouldParseNextRunAtFromIso8601() {
        DailyExecutionRow row = new DailyExecutionRow(1L, "2025-01-01", 10, 10, 0, null, null);
        when(analyticsRepository.findDailyRows(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of(row));

        MlContracts.PredictionItem mlItem = new MlContracts.PredictionItem(
                1L, "2025-06-15T08:00:00Z", 0.1, null, 0.9
        );
        when(mlClient.getPredictions(any()))
                .thenReturn(new MlContracts.PredictionResponse(List.of(mlItem)));

        List<Prediction> result = predictionService.getPredictions(1L, 1L);

        assertThat(result.get(0).nextRunAt())
                .isEqualTo(Instant.parse("2025-06-15T08:00:00Z"));
    }

    @Test
    void shouldSetNextRunAtToNullWhenParsingFails() {
        DailyExecutionRow row = new DailyExecutionRow(1L, "2025-01-01", 10, 10, 0, null, null);
        when(analyticsRepository.findDailyRows(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of(row));

        MlContracts.PredictionItem mlItem = new MlContracts.PredictionItem(
                1L, "NOT_A_DATE", 0.1, null, 0.9
        );
        when(mlClient.getPredictions(any()))
                .thenReturn(new MlContracts.PredictionResponse(List.of(mlItem)));

        List<Prediction> result = predictionService.getPredictions(1L, 1L);

        assertThat(result.get(0).nextRunAt()).isNull();
    }

    @Test
    void shouldHandleNullNextRunAt() {
        DailyExecutionRow row = new DailyExecutionRow(1L, "2025-01-01", 10, 10, 0, null, null);
        when(analyticsRepository.findDailyRows(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of(row));

        MlContracts.PredictionItem mlItem = new MlContracts.PredictionItem(
                1L, null, 0.1, null, 0.9
        );
        when(mlClient.getPredictions(any()))
                .thenReturn(new MlContracts.PredictionResponse(List.of(mlItem)));

        List<Prediction> result = predictionService.getPredictions(1L, 1L);

        assertThat(result.get(0).nextRunAt()).isNull();
    }

    @Test
    void shouldHandleBlankNextRunAt() {
        DailyExecutionRow row = new DailyExecutionRow(1L, "2025-01-01", 10, 10, 0, null, null);
        when(analyticsRepository.findDailyRows(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of(row));

        MlContracts.PredictionItem mlItem = new MlContracts.PredictionItem(
                1L, "   ", 0.1, null, 0.9
        );
        when(mlClient.getPredictions(any()))
                .thenReturn(new MlContracts.PredictionResponse(List.of(mlItem)));

        List<Prediction> result = predictionService.getPredictions(1L, 1L);

        assertThat(result.get(0).nextRunAt()).isNull();
    }

    @Test
    void shouldHandleNullableExpectedDurationMs() {
        DailyExecutionRow row = new DailyExecutionRow(1L, "2025-01-01", 10, 10, 0, null, null);
        when(analyticsRepository.findDailyRows(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of(row));

        MlContracts.PredictionItem mlItem = new MlContracts.PredictionItem(
                1L, "2025-06-01T00:00:00Z", 0.1, null, 0.9
        );
        when(mlClient.getPredictions(any()))
                .thenReturn(new MlContracts.PredictionResponse(List.of(mlItem)));

        List<Prediction> result = predictionService.getPredictions(1L, 1L);

        assertThat(result.get(0).expectedDurationMs()).isNull();
    }

    @Test
    void shouldMapMultiplePredictions() {
        DailyExecutionRow row1 = new DailyExecutionRow(1L, "2025-01-01", 10, 9, 1, 100.0, null);
        DailyExecutionRow row2 = new DailyExecutionRow(1L, "2025-01-02", 20, 18, 2, 110.0, null);
        when(analyticsRepository.findDailyRows(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of(row1, row2));

        MlContracts.PredictionItem item1 = new MlContracts.PredictionItem(1L, null, 0.1, null, 0.9);
        MlContracts.PredictionItem item2 = new MlContracts.PredictionItem(2L, null, 0.2, null, 0.8);
        when(mlClient.getPredictions(any()))
                .thenReturn(new MlContracts.PredictionResponse(List.of(item1, item2)));

        List<Prediction> result = predictionService.getPredictions(1L, null);

        assertThat(result).hasSize(2);
    }

    // ── getAnomalies ──────────────────────────────────────────────────────

    @Test
    void shouldReturnEmptyAnomaliesWhenHistoryIsEmpty() {
        when(analyticsRepository.findDailyRows(1L, 42L, 90)).thenReturn(List.of());

        List<AnomalyDetection> result =
                predictionService.getAnomalies(1L, 42L, "2025-01-01", "2025-01-31");

        assertThat(result).isEmpty();
        verify(mlClient, never()).getAnomalies(any());
    }

    @Test
    void shouldCallMlClientWithCorrectAnomalyRequest() {
        DailyExecutionRow row = new DailyExecutionRow(1L, "2025-01-01", 10, 9, 1, 100.0, 150.0);
        when(analyticsRepository.findDailyRows(1L, 1L, 90)).thenReturn(List.of(row));

        MlContracts.AnomalyItem mlItem = new MlContracts.AnomalyItem(
                1L, "duration_ms", 5000.0, 1000.0, 3000.0, "HIGH", "2025-05-15T10:00:00Z"
        );
        when(mlClient.getAnomalies(any()))
                .thenReturn(new MlContracts.AnomalyResponse(List.of(mlItem)));

        List<AnomalyDetection> result =
                predictionService.getAnomalies(1L, 1L, "2025-01-01", "2025-01-31");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).jobId()).isEqualTo(1L);
        assertThat(result.get(0).metric()).isEqualTo("duration_ms");
        assertThat(result.get(0).severity()).isEqualTo(AnomalySeverity.HIGH);
    }

    @Test
    void shouldParseSeverityCaseInsensitive() {
        DailyExecutionRow row = new DailyExecutionRow(1L, "2025-01-01", 10, 9, 1, null, null);
        when(analyticsRepository.findDailyRows(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of(row));

        MlContracts.AnomalyItem mlItem = new MlContracts.AnomalyItem(
                1L, "duration_ms", 5000.0, 1000.0, 3000.0, "critical", "2025-05-15T10:00:00Z"
        );
        when(mlClient.getAnomalies(any()))
                .thenReturn(new MlContracts.AnomalyResponse(List.of(mlItem)));

        List<AnomalyDetection> result =
                predictionService.getAnomalies(1L, 1L, "2025-01-01", "2025-01-31");

        assertThat(result.get(0).severity()).isEqualTo(AnomalySeverity.CRITICAL);
    }

    @Test
    void shouldFallbackToLowWhenSeverityIsUnknown() {
        DailyExecutionRow row = new DailyExecutionRow(1L, "2025-01-01", 10, 9, 1, null, null);
        when(analyticsRepository.findDailyRows(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of(row));

        MlContracts.AnomalyItem mlItem = new MlContracts.AnomalyItem(
                1L, "duration_ms", 5000.0, 1000.0, 3000.0, "UNKNOWN_SEVERITY", "2025-05-15T10:00:00Z"
        );
        when(mlClient.getAnomalies(any()))
                .thenReturn(new MlContracts.AnomalyResponse(List.of(mlItem)));

        List<AnomalyDetection> result =
                predictionService.getAnomalies(1L, 1L, "2025-01-01", "2025-01-31");

        assertThat(result.get(0).severity()).isEqualTo(AnomalySeverity.LOW);
    }

    @Test
    void shouldParseDetectedAtFromIso8601() {
        DailyExecutionRow row = new DailyExecutionRow(1L, "2025-01-01", 10, 9, 1, null, null);
        when(analyticsRepository.findDailyRows(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of(row));

        MlContracts.AnomalyItem mlItem = new MlContracts.AnomalyItem(
                1L, "duration_ms", 5000.0, 1000.0, 3000.0, "LOW", "2025-05-15T12:30:00Z"
        );
        when(mlClient.getAnomalies(any()))
                .thenReturn(new MlContracts.AnomalyResponse(List.of(mlItem)));

        List<AnomalyDetection> result =
                predictionService.getAnomalies(1L, 1L, "2025-01-01", "2025-01-31");

        assertThat(result.get(0).detectedAt())
                .isEqualTo(Instant.parse("2025-05-15T12:30:00Z"));
    }

    @Test
    void shouldFallbackToNowWhenDetectedAtIsInvalid() {
        DailyExecutionRow row = new DailyExecutionRow(1L, "2025-01-01", 10, 9, 1, null, null);
        when(analyticsRepository.findDailyRows(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of(row));

        MlContracts.AnomalyItem mlItem = new MlContracts.AnomalyItem(
                1L, "duration_ms", 5000.0, 1000.0, 3000.0, "LOW", "not-a-date"
        );
        when(mlClient.getAnomalies(any()))
                .thenReturn(new MlContracts.AnomalyResponse(List.of(mlItem)));

        Instant beforeCall = Instant.now();
        List<AnomalyDetection> result =
                predictionService.getAnomalies(1L, 1L, "2025-01-01", "2025-01-31");

        // Should be close to now (within a few seconds of when we called)
        assertThat(result.get(0).detectedAt())
                .isAfterOrEqualTo(beforeCall)
                .isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void shouldPassHistoryDaysAs90() {
        when(analyticsRepository.findDailyRows(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of());

        predictionService.getPredictions(1L, 1L);
        verify(analyticsRepository).findDailyRows(1L, 1L, 90);

        predictionService.getAnomalies(1L, 1L, "2025-01-01", "2025-01-31");
        verify(analyticsRepository).findDailyRows(1L, 1L, 90);
    }

    @Test
    void shouldBuildCorrectAnomalyRequestWithFromTo() {
        DailyExecutionRow row = new DailyExecutionRow(1L, "2025-01-01", 10, 9, 1, null, null);
        when(analyticsRepository.findDailyRows(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of(row));
        when(mlClient.getAnomalies(any()))
                .thenReturn(new MlContracts.AnomalyResponse(List.of()));

        predictionService.getAnomalies(5L, 10L, "2025-03-01", "2025-03-31");

        verify(mlClient).getAnomalies(argThat(req ->
                req.orgId().equals(5L) &&
                req.jobId().equals(10L) &&
                req.from().equals("2025-03-01") &&
                req.to().equals("2025-03-31")
        ));
    }

    @Test
    void shouldWorkWhenMlClientReturnsEmptyFallback() {
        DailyExecutionRow row = new DailyExecutionRow(1L, "2025-01-01", 10, 9, 1, null, null);
        when(analyticsRepository.findDailyRows(anyLong(), anyLong(), anyInt()))
                .thenReturn(List.of(row));
        when(mlClient.getPredictions(any()))
                .thenReturn(new MlContracts.PredictionResponse(List.of()));

        List<Prediction> result = predictionService.getPredictions(1L, 1L);

        assertThat(result).isEmpty();
    }
}
