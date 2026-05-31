package com.cromp.analytics.infrastructure.web.controller;

import com.cromp.analytics.api.dto.AnalyticsDtos;
import com.cromp.analytics.api.mapper.AnalyticsMapper;
import com.cromp.analytics.application.service.AnalyticsService;
import com.cromp.analytics.application.service.PredictionService;
import com.cromp.analytics.domain.model.AnalyticsPeriod;
import com.cromp.analytics.domain.model.AnomalyDetection;
import com.cromp.analytics.domain.model.ExecutionSummary;
import com.cromp.analytics.domain.model.Prediction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * возвращает 200 с пустым payload если данных нет (не 404) —
 * отсутствие данных не ошибка, это нормальное состояние для новой организации.
 */
@Slf4j
@Tag(name = "Analytics", description = "Аналитика выполнения задач: сводки, прогнозы, аномалии")
@RestController
@RequestMapping("/api/v1/organizations/{orgId}/analytics")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final PredictionService predictionService;
    private final AnalyticsMapper mapper;

    // ── Summary ───────────────────────────────────────────────────────────────

    @Operation(summary = "Получить сводку по выполнениям",
            description = "Возвращает агрегированную статистику выполнений за период. Если данных нет — возвращает 200 с нулевыми значениями.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Сводка (может быть пустой для новых организаций)"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав analytics:read")
    })
    @GetMapping("/executions/summary")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'analytics:read')")
    public ResponseEntity<AnalyticsDtos.SummaryResponse> getSummary(
            @Parameter(description = "ID организации") @PathVariable Long orgId,
            @Parameter(description = "Период: 1d, 7d, 30d, 90d (по умолчанию 7d)")
            @RequestParam(defaultValue = "7d") String period,
            @Parameter(description = "Опциональный фильтр по ID задачи")
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

    @Operation(summary = "Получить прогнозы по задачам",
            description = "Возвращает ML-прогнозы: ожидаемое время следующего запуска, вероятность ошибки, предсказываемую длительность.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Прогнозы (может быть пустым)"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав analytics:read")
    })
    @GetMapping("/predictions")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'analytics:read')")
    public ResponseEntity<AnalyticsDtos.PredictionsResponse> getPredictions(
            @Parameter(description = "ID организации") @PathVariable Long orgId,
            @Parameter(description = "Опциональный фильтр по ID задачи")
            @RequestParam(required = false) Long jobId) {

        List<Prediction> predictions = predictionService.getPredictions(orgId, jobId);
        return ResponseEntity.ok(mapper.toPredictionsResponse(orgId, jobId, predictions));
    }

    // ── Anomalies ─────────────────────────────────────────────────────────────

    @Operation(summary = "Получить обнаруженные аномалии",
            description = "Возвращает список аномалий в выполнениях за указанный период. По умолчанию период — последние 30 дней.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список аномалий (может быть пустым)"),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован"),
            @ApiResponse(responseCode = "403", description = "Нет прав analytics:read")
    })
    @GetMapping("/anomalies")
    @PreAuthorize("@permissionCheckerPort.hasPermission(authentication.principal, 'analytics:read')")
    public ResponseEntity<AnalyticsDtos.AnomaliesResponse> getAnomalies(
            @Parameter(description = "ID организации") @PathVariable Long orgId,
            @Parameter(description = "Опциональный фильтр по ID задачи")
            @RequestParam(required = false) Long jobId,
            @Parameter(description = "Начало периода (ISO date, по умолчанию 30 дней назад)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Конец периода (ISO date, по умолчанию сегодня)")
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