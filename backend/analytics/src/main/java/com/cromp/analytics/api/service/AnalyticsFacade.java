package com.cromp.analytics.api.service;

import com.cromp.analytics.api.dto.response.AnomalyResponse;
import com.cromp.analytics.api.dto.response.DailyExecutionStatsResponse;
import com.cromp.analytics.api.dto.response.ExecutionSummaryResponse;
import com.cromp.analytics.api.dto.response.PredictionResponse;

import java.time.Instant;
import java.util.List;

public interface AnalyticsFacade {
    ExecutionSummaryResponse executionSummary(Long organizationId, Long jobId, Instant from, Instant to);
    List<DailyExecutionStatsResponse> dailyStats(Long organizationId, Long jobId);
    List<PredictionResponse> predictions(Long organizationId, Long jobId);
    List<AnomalyResponse> anomalies(Long organizationId, Long jobId, Instant from, Instant to, String severity);
    void refreshDailyStats();
}
