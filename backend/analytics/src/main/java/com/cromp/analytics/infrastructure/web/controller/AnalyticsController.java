package com.cromp.analytics.infrastructure.web.controller;

import com.cromp.analytics.api.dto.AnalyticsDtos;
import com.cromp.analytics.api.mapper.AnalyticsMapper;
import com.cromp.analytics.application.service.AnalyticsService;
import com.cromp.analytics.application.service.PredictionService;
import com.cromp.analytics.domain.model.AnalyticsPeriod;
import com.cromp.analytics.domain.model.AnomalyDetection;
import com.cromp.analytics.domain.model.ExecutionSummary;
import com.cromp.analytics.domain.model.Prediction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST-контроллер аналитики.
 *
 * Все эндпоинты требуют права {@code analytics:read} и контекст организации.
 * organizationId берётся из path — проверка принадлежности пользователя
 * к организации выполняется через {@code @PreAuthorize}.
 *
 * Возвращает 200 с пустым payload если данных нет (не 404) —
 * отсутствие данных не ошибка, это нормальное состояние для новой организации.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/organizations/{orgId}/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final PredictionService predictionService;
    private final AnalyticsMapper mapper;

    // ── Summary ───────────────────────────────────────────────────────────────

    /**
     * GET /api/v1/organizations/{orgId}/analytics/executions/summary
     *
     * @param period  период: 1d, 7d, 30d, 90d (default: 7d)
     * @param jobId   опционально — фильтр по задаче
     */
    @GetMapping("/executions/summary")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'analytics:read')")
    public ResponseEntity<AnalyticsDtos.SummaryResponse> getSummary(
            @PathVariable Long orgId,
            @RequestParam(defaultValue = "7d") String period,
            @RequestParam(required = false) Long jobId) {

        AnalyticsPeriod analyticsPeriod = parsePeriod(period);

        Optional<ExecutionSummary> summary = jobId != null
                ? analyticsService.getSummaryByJob(orgId, jobId, analyticsPeriod)
                : analyticsService.getSummary(orgId, analyticsPeriod);

        return summary
                .map(s -> ResponseEntity.ok(mapper.toSummaryResponse(s)))
                .orElse(ResponseEntity.ok(emptysummary(orgId, jobId, period)));
    }

    // ── Predictions ───────────────────────────────────────────────────────────

    /**
     * GET /api/v1/organizations/{orgId}/analytics/predictions
     *
     * @param jobId опционально — прогноз для конкретной задачи
     */
    @GetMapping("/predictions")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'analytics:read')")
    public ResponseEntity<AnalyticsDtos.PredictionsResponse> getPredictions(
            @PathVariable Long orgId,
            @RequestParam(required = false) Long jobId) {

        List<Prediction> predictions = predictionService.getPredictions(orgId, jobId);
        return ResponseEntity.ok(mapper.toPredictionsResponse(orgId, jobId, predictions));
    }

    // ── Anomalies ─────────────────────────────────────────────────────────────

    /**
     * GET /api/v1/organizations/{orgId}/analytics/anomalies
     *
     * @param from     начало периода (ISO date, default: 30 дней назад)
     * @param to       конец периода (ISO date, default: сегодня)
     * @param jobId    опционально — фильтр по задаче
     */
    @GetMapping("/anomalies")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'analytics:read')")
    public ResponseEntity<AnalyticsDtos.AnomaliesResponse> getAnomalies(
            @PathVariable Long orgId,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        String fromStr = (from != null ? from : LocalDate.now().minusDays(30)).toString();
        String toStr   = (to   != null ? to   : LocalDate.now()).toString();

        List<AnomalyDetection> anomalies =
                predictionService.getAnomalies(orgId, jobId, fromStr, toStr);

        return ResponseEntity.ok(
                mapper.toAnomaliesResponse(orgId, jobId, fromStr, toStr, anomalies));
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private AnalyticsPeriod parsePeriod(String period) {
        try {
            return AnalyticsPeriod.fromAlias(period);
        } catch (IllegalArgumentException e) {
            log.warn("[analytics] unknown period='{}', defaulting to SEVEN_DAYS", period);
            return AnalyticsPeriod.SEVEN_DAYS;
        }
    }

    /** Пустой ответ когда данных нет — не 404, а 200 с нулями. */
    private AnalyticsDtos.SummaryResponse emptysummary(Long orgId, Long jobId, String period) {
        return new AnalyticsDtos.SummaryResponse(
                orgId, jobId, period, 0, 0, 0, 0.0, null, null, null);
    }
}