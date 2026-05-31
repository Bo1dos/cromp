package com.cromp.analytics.infrastructure.mlclient.client;

import com.cromp.analytics.infrastructure.mlclient.contract.MlContracts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.ConnectException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MlClientImplTest {

    @Mock
    private RestClient restClient;

    private MlClientImpl mlClient;

    // RestClient fluent API mocks
    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private RestClient.RequestBodySpec requestBodySpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        mlClient = new MlClientImpl(restClient);
        setupRestClientMock();
    }

    @SuppressWarnings("unchecked")
    private void setupRestClientMock() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }

    // ── getPredictions ────────────────────────────────────────────────────

    @Test
    void shouldReturnPredictionsOnSuccess() {
        MlContracts.PredictionRequest request =
                new MlContracts.PredictionRequest(1L, 42L, List.of());

        MlContracts.PredictionItem item = new MlContracts.PredictionItem(
                42L, "2025-06-01T00:00:00Z", 0.1, 500L, 0.95
        );
        MlContracts.PredictionResponse expected =
                new MlContracts.PredictionResponse(List.of(item));

        when(responseSpec.body(eq(MlContracts.PredictionResponse.class)))
                .thenReturn(expected);

        MlContracts.PredictionResponse result = mlClient.getPredictions(request);

        assertThat(result).isEqualTo(expected);
        assertThat(result.predictions()).hasSize(1);
    }

    @Test
    void shouldReturnEmptyFallbackWhenResponseIsNull() {
        MlContracts.PredictionRequest request =
                new MlContracts.PredictionRequest(1L, null, List.of());

        when(responseSpec.body(eq(MlContracts.PredictionResponse.class)))
                .thenReturn(null);

        MlContracts.PredictionResponse result = mlClient.getPredictions(request);

        assertThat(result.predictions()).isEmpty();
    }

    @Test
    void shouldReturnEmptyFallbackWhenPredictionsListIsNull() {
        MlContracts.PredictionRequest request =
                new MlContracts.PredictionRequest(1L, null, List.of());

        when(responseSpec.body(eq(MlContracts.PredictionResponse.class)))
                .thenReturn(new MlContracts.PredictionResponse(null));

        MlContracts.PredictionResponse result = mlClient.getPredictions(request);

        assertThat(result.predictions()).isEmpty();
    }

    @Test
    void shouldReturnEmptyFallbackOnResourceAccessException() {
        MlContracts.PredictionRequest request =
                new MlContracts.PredictionRequest(1L, 1L, List.of());

        when(responseSpec.body(eq(MlContracts.PredictionResponse.class)))
                .thenThrow(new ResourceAccessException("Connection refused",
                        new ConnectException("Connection refused")));

        MlContracts.PredictionResponse result = mlClient.getPredictions(request);

        assertThat(result.predictions()).isEmpty();
    }

    @Test
    void shouldReturnEmptyFallbackOnGenericException() {
        MlContracts.PredictionRequest request =
                new MlContracts.PredictionRequest(1L, 1L, List.of());

        when(responseSpec.body(eq(MlContracts.PredictionResponse.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        MlContracts.PredictionResponse result = mlClient.getPredictions(request);

        assertThat(result.predictions()).isEmpty();
    }

    @Test
    void shouldUseCorrectPredictionsPath() {
        MlContracts.PredictionRequest request =
                new MlContracts.PredictionRequest(1L, null, List.of());
        when(responseSpec.body(eq(MlContracts.PredictionResponse.class)))
                .thenReturn(new MlContracts.PredictionResponse(List.of()));

        mlClient.getPredictions(request);

        verify(requestBodyUriSpec).uri("/predictions");
    }

    @Test
    void shouldPassRequestBodyCorrectly() {
        MlContracts.PredictionRequest request =
                new MlContracts.PredictionRequest(10L, 20L, List.of());
        when(responseSpec.body(eq(MlContracts.PredictionResponse.class)))
                .thenReturn(new MlContracts.PredictionResponse(List.of()));

        mlClient.getPredictions(request);

        verify(requestBodySpec).body(request);
    }

    // ── getAnomalies ──────────────────────────────────────────────────────

    @Test
    void shouldReturnAnomaliesOnSuccess() {
        MlContracts.AnomalyRequest request = new MlContracts.AnomalyRequest(
                1L, 42L, "2025-01-01", "2025-01-31", List.of()
        );

        MlContracts.AnomalyItem item = new MlContracts.AnomalyItem(
                42L, "duration_ms", 5000.0, 1000.0, 3000.0, "HIGH", "2025-05-15T10:00:00Z"
        );
        MlContracts.AnomalyResponse expected = new MlContracts.AnomalyResponse(List.of(item));

        when(responseSpec.body(eq(MlContracts.AnomalyResponse.class)))
                .thenReturn(expected);

        MlContracts.AnomalyResponse result = mlClient.getAnomalies(request);

        assertThat(result).isEqualTo(expected);
        assertThat(result.anomalies()).hasSize(1);
    }

    @Test
    void shouldReturnEmptyFallbackWhenAnomaliesResponseIsNull() {
        MlContracts.AnomalyRequest request = new MlContracts.AnomalyRequest(
                1L, null, "2025-01-01", "2025-01-31", List.of()
        );

        when(responseSpec.body(eq(MlContracts.AnomalyResponse.class)))
                .thenReturn(null);

        MlContracts.AnomalyResponse result = mlClient.getAnomalies(request);

        assertThat(result.anomalies()).isEmpty();
    }

    @Test
    void shouldReturnEmptyFallbackWhenAnomaliesListIsNull() {
        MlContracts.AnomalyRequest request = new MlContracts.AnomalyRequest(
                1L, null, "2025-01-01", "2025-01-31", List.of()
        );

        when(responseSpec.body(eq(MlContracts.AnomalyResponse.class)))
                .thenReturn(new MlContracts.AnomalyResponse(null));

        MlContracts.AnomalyResponse result = mlClient.getAnomalies(request);

        assertThat(result.anomalies()).isEmpty();
    }

    @Test
    void shouldReturnEmptyFallbackOnResourceAccessExceptionForAnomalies() {
        MlContracts.AnomalyRequest request = new MlContracts.AnomalyRequest(
                1L, 1L, "2025-01-01", "2025-01-31", List.of()
        );

        when(responseSpec.body(eq(MlContracts.AnomalyResponse.class)))
                .thenThrow(new ResourceAccessException("Timeout",
                        new java.net.SocketTimeoutException("Timeout")));

        MlContracts.AnomalyResponse result = mlClient.getAnomalies(request);

        assertThat(result.anomalies()).isEmpty();
    }

    @Test
    void shouldReturnEmptyFallbackOnGenericExceptionForAnomalies() {
        MlContracts.AnomalyRequest request = new MlContracts.AnomalyRequest(
                1L, 1L, "2025-01-01", "2025-01-31", List.of()
        );

        when(responseSpec.body(eq(MlContracts.AnomalyResponse.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        MlContracts.AnomalyResponse result = mlClient.getAnomalies(request);

        assertThat(result.anomalies()).isEmpty();
    }

    @Test
    void shouldUseCorrectAnomaliesPath() {
        MlContracts.AnomalyRequest request = new MlContracts.AnomalyRequest(
                1L, null, "2025-01-01", "2025-01-31", List.of()
        );
        when(responseSpec.body(eq(MlContracts.AnomalyResponse.class)))
                .thenReturn(new MlContracts.AnomalyResponse(List.of()));

        mlClient.getAnomalies(request);

        verify(requestBodyUriSpec).uri("/anomalies");
    }
}
