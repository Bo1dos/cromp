package com.cromp.analytics.infrastructure.web.controller;

import com.cromp.analytics.api.dto.AnalyticsDtos;
import com.cromp.analytics.api.mapper.AnalyticsMapper;
import com.cromp.analytics.application.service.AnalyticsService;
import com.cromp.analytics.application.service.PredictionService;
import com.cromp.analytics.domain.model.AnalyticsPeriod;
import com.cromp.analytics.domain.model.AnomalyDetection;
import com.cromp.analytics.domain.model.ExecutionSummary;
import com.cromp.analytics.domain.model.Prediction;
import com.cromp.analytics.domain.model.enums.AnomalySeverity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AnalyticsService analyticsService;

    @MockitoBean
    private PredictionService predictionService;

    @MockitoBean
    private AnalyticsMapper mapper;

    // ── getSummary ────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void shouldReturnSummaryWithDefaultPeriod() throws Exception {
        ExecutionSummary summary = summary(1L, null, AnalyticsPeriod.SEVEN_DAYS, 100, 80, 20);
        when(analyticsService.getSummary(1L, AnalyticsPeriod.SEVEN_DAYS))
                .thenReturn(Optional.of(summary));
        when(mapper.toSummaryResponse(summary)).thenReturn(summaryResponse(1L, null, "SEVEN_DAYS"));

        mockMvc.perform(get("/api/v1/organizations/1/analytics/executions/summary")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationId").value(1))
                .andExpect(jsonPath("$.period").value("SEVEN_DAYS"))
                .andExpect(jsonPath("$.total").value(100));
    }

    @Test
    @WithMockUser
    void shouldReturnEmptySummaryWhenNoData() throws Exception {
        when(analyticsService.getSummary(1L, AnalyticsPeriod.SEVEN_DAYS))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/organizations/1/analytics/executions/summary")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationId").value(1))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.succeeded").value(0))
                .andExpect(jsonPath("$.failed").value(0))
                .andExpect(jsonPath("$.errorRate").value(0.0));
    }

    @Test
    @WithMockUser
    void shouldFallbackToSevenDaysOnInvalidPeriod() throws Exception {
        when(analyticsService.getSummary(1L, AnalyticsPeriod.SEVEN_DAYS))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/organizations/1/analytics/executions/summary")
                        .param("period", "invalid")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({"1d", "7d", "30d", "90d"})
    @WithMockUser
    void shouldAcceptValidPeriods(String period) throws Exception {
        when(analyticsService.getSummary(anyLong(), any()))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/organizations/1/analytics/executions/summary")
                        .param("period", period)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldUseJobLevelSummaryWhenJobIdProvided() throws Exception {
        ExecutionSummary summary = summary(1L, 42L, AnalyticsPeriod.SEVEN_DAYS, 10, 8, 2);
        when(analyticsService.getSummaryByJob(1L, 42L, AnalyticsPeriod.SEVEN_DAYS))
                .thenReturn(Optional.of(summary));
        when(mapper.toSummaryResponse(summary))
                .thenReturn(summaryResponse(1L, 42L, "SEVEN_DAYS"));

        mockMvc.perform(get("/api/v1/organizations/1/analytics/executions/summary")
                        .param("jobId", "42")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(42));
    }

    @Test
    @WithMockUser
    void shouldReturn200WhenSummaryPresent() throws Exception {
        ExecutionSummary summary = summary(1L, null, AnalyticsPeriod.SEVEN_DAYS, 50, 45, 5);
        when(analyticsService.getSummary(anyLong(), any()))
                .thenReturn(Optional.of(summary));
        when(mapper.toSummaryResponse(summary)).thenReturn(summaryResponse(1L, null, "SEVEN_DAYS"));

        mockMvc.perform(get("/api/v1/organizations/1/analytics/executions/summary")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // ── getPredictions ────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void shouldReturnPredictions() throws Exception {
        List<Prediction> predictions = List.of(
                new Prediction(1L, Instant.now(), 0.1, 100L, 0.9)
        );
        when(predictionService.getPredictions(1L, null)).thenReturn(predictions);
        AnalyticsDtos.PredictionsResponse response = new AnalyticsDtos.PredictionsResponse(
                1L, null, List.of(
                new AnalyticsDtos.PredictionItem(1L, Instant.now(), 0.1, 100L, 0.9)
        ));
        when(mapper.toPredictionsResponse(eq(1L), eq((Long) null), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/organizations/1/analytics/predictions")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationId").value(1))
                .andExpect(jsonPath("$.predictions").isArray());
    }

    @Test
    @WithMockUser
    void shouldReturnEmptyPredictionsWhenNoneFound() throws Exception {
        when(predictionService.getPredictions(1L, null)).thenReturn(List.of());
        when(mapper.toPredictionsResponse(eq(1L), eq((Long) null), any()))
                .thenReturn(new AnalyticsDtos.PredictionsResponse(1L, null, List.of()));

        mockMvc.perform(get("/api/v1/organizations/1/analytics/predictions")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.predictions").isEmpty());
    }

    @Test
    @WithMockUser
    void shouldPassJobIdToPredictions() throws Exception {
        when(predictionService.getPredictions(5L, 10L)).thenReturn(List.of());
        when(mapper.toPredictionsResponse(eq(5L), eq(10L), any()))
                .thenReturn(new AnalyticsDtos.PredictionsResponse(5L, 10L, List.of()));

        mockMvc.perform(get("/api/v1/organizations/5/analytics/predictions")
                        .param("jobId", "10")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(predictionService).getPredictions(5L, 10L);
    }

    // ── getAnomalies ──────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void shouldReturnAnomalies() throws Exception {
        when(predictionService.getAnomalies(eq(1L), eq((Long) null), anyString(), anyString()))
                .thenReturn(List.of());
        when(mapper.toAnomaliesResponse(eq(1L), eq((Long) null), anyString(), anyString(), any()))
                .thenReturn(new AnalyticsDtos.AnomaliesResponse(1L, null, "2025-01-01", "2025-06-01", List.of()));

        mockMvc.perform(get("/api/v1/organizations/1/analytics/anomalies")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anomalies").isArray());
    }

    @Test
    @WithMockUser
    void shouldReturnEmptyAnomaliesWhenNoneFound() throws Exception {
        when(predictionService.getAnomalies(anyLong(), any(), anyString(), anyString()))
                .thenReturn(List.of());
        when(mapper.toAnomaliesResponse(anyLong(), any(), anyString(), anyString(), any()))
                .thenReturn(new AnalyticsDtos.AnomaliesResponse(1L, null, "2025-01-01", "2025-06-01", List.of()));

        mockMvc.perform(get("/api/v1/organizations/1/analytics/anomalies")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anomalies").isEmpty());
    }

    @Test
    @WithMockUser
    void shouldPassFromToParamsToAnomalies() throws Exception {
        when(predictionService.getAnomalies(eq(1L), eq((Long) null), anyString(), anyString()))
                .thenReturn(List.of());
        when(mapper.toAnomaliesResponse(anyLong(), any(), anyString(), anyString(), any()))
                .thenReturn(new AnalyticsDtos.AnomaliesResponse(1L, null, "2025-05-01", "2025-05-15", List.of()));

        mockMvc.perform(get("/api/v1/organizations/1/analytics/anomalies")
                        .param("from", "2025-05-01")
                        .param("to", "2025-05-15")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldPassJobIdToAnomalies() throws Exception {
        when(predictionService.getAnomalies(eq(3L), eq(7L), anyString(), anyString()))
                .thenReturn(List.of());
        when(mapper.toAnomaliesResponse(eq(3L), eq(7L), anyString(), anyString(), any()))
                .thenReturn(new AnalyticsDtos.AnomaliesResponse(3L, 7L, "2025-01-01", "2025-06-01", List.of()));

        mockMvc.perform(get("/api/v1/organizations/3/analytics/anomalies")
                        .param("jobId", "7")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(predictionService).getAnomalies(eq(3L), eq(7L), anyString(), anyString());
    }

    // ── Security ──────────────────────────────────────────────────────────

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/1/analytics/executions/summary")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401ForPredictionsWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/1/analytics/predictions")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401ForAnomaliesWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/1/analytics/anomalies")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private ExecutionSummary summary(Long orgId, Long jobId, AnalyticsPeriod period,
                                      long total, long succeeded, long failed) {
        return new ExecutionSummary(
                orgId, jobId, period, total, succeeded, failed,
                -1.0, 100.0, 200.0, Instant.now()
        );
    }

    private AnalyticsDtos.SummaryResponse summaryResponse(Long orgId, Long jobId, String period) {
        return new AnalyticsDtos.SummaryResponse(
                orgId, jobId, period, 100, 80, 20, 0.20, 100.0, 200.0, Instant.now()
        );
    }
}
