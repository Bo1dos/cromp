package com.cromp.analytics.infrastructure.web.controller;

import com.cromp.analytics.api.dto.response.AnomalyResponse;
import com.cromp.analytics.api.dto.response.DailyExecutionStatsResponse;
import com.cromp.analytics.api.dto.response.ExecutionSummaryResponse;
import com.cromp.analytics.api.dto.response.PredictionResponse;
import com.cromp.analytics.api.service.AnalyticsFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsFacade analyticsFacade;

    @GetMapping("/executions/summary")
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public ExecutionSummaryResponse summary(@PathVariable Long organizationId,
                                            @RequestParam(required = false) Long jobId,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return analyticsFacade.executionSummary(organizationId, jobId, from, to);
    }

    @GetMapping("/executions/daily")
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public List<DailyExecutionStatsResponse> daily(@PathVariable Long organizationId,
                                                   @RequestParam(required = false) Long jobId) {
        return analyticsFacade.dailyStats(organizationId, jobId);
    }

    @GetMapping("/predictions")
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public List<PredictionResponse> predictions(@PathVariable Long organizationId,
                                                @RequestParam(required = false) Long jobId) {
        return analyticsFacade.predictions(organizationId, jobId);
    }

    @GetMapping("/anomalies")
    @PreAuthorize("@permissionCheckerPort.isMember(authentication.principal, #organizationId)")
    public List<AnomalyResponse> anomalies(@PathVariable Long organizationId,
                                           @RequestParam(required = false) Long jobId,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
                                           @RequestParam(required = false) String severity) {
        return analyticsFacade.anomalies(organizationId, jobId, from, to, severity);
    }

    @PostMapping("/refresh")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, #organizationId, 'analytics:refresh')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void refresh(@PathVariable Long organizationId) {
        analyticsFacade.refreshDailyStats();
    }
}
