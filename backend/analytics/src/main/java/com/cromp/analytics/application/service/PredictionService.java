package com.cromp.analytics.application.service;

import com.cromp.analytics.domain.model.AnomalyDetection;
import com.cromp.analytics.domain.model.Prediction;
import com.cromp.analytics.domain.model.enums.AnomalySeverity;
import com.cromp.analytics.domain.repository.AnalyticsRepository;
import com.cromp.analytics.domain.repository.AnalyticsRepository.DailyExecutionRow;
import com.cromp.analytics.infrastructure.mlclient.client.MlClient;
import com.cromp.analytics.infrastructure.mlclient.contract.MlContracts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Сервис прогнозов и обнаружения аномалий.
 *
 * Алгоритм:
 *   1. Загрузить исторические строки из MV через {@link AnalyticsRepository}.
 *   2. Сформировать запрос к ML-сервису.
 *   3. Вызвать {@link MlClient} (с встроенным fallback на пустой список).
 *   4. Смапить ответ в доменные объекты.
 *
 * Если ML недоступен — {@link MlClient} вернёт пустой список, сервис вернёт
 * пустой список наружу. Клиент получит 200 с пустым массивом, а не 503.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    /** Глубина истории, передаваемой в ML-сервис. */
    private static final int HISTORY_DAYS = 90;

    private final AnalyticsRepository analyticsRepository;
    private final MlClient mlClient;

    // ── Predictions ───────────────────────────────────────────────────────────

    @Cacheable(
            cacheNames = "analytics.predictions",
            key = "#organizationId + ':' + (#jobId != null ? #jobId : 'all')"
    )
    public List<Prediction> getPredictions(Long organizationId, Long jobId) {
        log.debug("[prediction] fetching predictions orgId={} jobId={}", organizationId, jobId);

        List<DailyExecutionRow> history =
                analyticsRepository.findDailyRows(organizationId, jobId, HISTORY_DAYS);

        if (history.isEmpty()) {
            log.info("[prediction] no history found, returning empty predictions orgId={}", organizationId);
            return List.of();
        }

        MlContracts.PredictionRequest request = new MlContracts.PredictionRequest(
                organizationId,
                jobId,
                toMlRows(history)
        );

        MlContracts.PredictionResponse response = mlClient.getPredictions(request);

        List<Prediction> predictions = response.predictions().stream()
                .map(this::toDomain)
                .toList();

        log.debug("[prediction] received {} prediction(s) orgId={}", predictions.size(), organizationId);
        return predictions;
    }

    // ── Anomalies ─────────────────────────────────────────────────────────────

    @Cacheable(
            cacheNames = "analytics.anomalies",
            key = "#organizationId + ':' + (#jobId != null ? #jobId : 'all') + ':' + #from + ':' + #to"
    )
    public List<AnomalyDetection> getAnomalies(Long organizationId, Long jobId,
                                                String from, String to) {
        log.debug("[anomaly] fetching anomalies orgId={} jobId={} from={} to={}",
                organizationId, jobId, from, to);

        List<DailyExecutionRow> history =
                analyticsRepository.findDailyRows(organizationId, jobId, HISTORY_DAYS);

        if (history.isEmpty()) {
            log.info("[anomaly] no history found, returning empty anomalies orgId={}", organizationId);
            return List.of();
        }

        MlContracts.AnomalyRequest request = new MlContracts.AnomalyRequest(
                organizationId,
                jobId,
                from,
                to,
                toMlRows(history)
        );

        MlContracts.AnomalyResponse response = mlClient.getAnomalies(request);

        List<AnomalyDetection> anomalies = response.anomalies().stream()
                .map(this::toDomain)
                .toList();

        log.debug("[anomaly] received {} anomal(ies) orgId={}", anomalies.size(), organizationId);
        return anomalies;
    }

    // ── Private mappers ───────────────────────────────────────────────────────

    private List<MlContracts.ExecutionRow> toMlRows(List<DailyExecutionRow> rows) {
        return rows.stream()
                .map(r -> new MlContracts.ExecutionRow(
                        r.jobId(),
                        r.day(),
                        r.total(),
                        r.succeeded(),
                        r.failed(),
                        r.avgDurationMs(),
                        r.p95DurationMs()
                ))
                .toList();
    }

    private Prediction toDomain(MlContracts.PredictionItem item) {
        Instant nextRunAt = null;
        if (item.nextRunAt() != null && !item.nextRunAt().isBlank()) {
            try {
                nextRunAt = Instant.parse(item.nextRunAt());
            } catch (Exception e) {
                log.warn("[prediction] failed to parse nextRunAt='{}' for jobId={}",
                        item.nextRunAt(), item.jobId());
            }
        }

        return new Prediction(
                item.jobId(),
                nextRunAt,
                item.failureProbability(),
                item.expectedDurationMs(),
                item.confidence()
        );
    }

    private AnomalyDetection toDomain(MlContracts.AnomalyItem item) {
        AnomalySeverity severity;
        try {
            severity = AnomalySeverity.valueOf(item.severity().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[anomaly] unknown severity='{}', defaulting to LOW", item.severity());
            severity = AnomalySeverity.LOW;
        }

        Instant detectedAt;
        try {
            detectedAt = Instant.parse(item.detectedAt());
        } catch (Exception e) {
            log.warn("[anomaly] failed to parse detectedAt='{}', using now", item.detectedAt());
            detectedAt = Instant.now();
        }

        return new AnomalyDetection(
                item.jobId(),
                item.metric(),
                item.value(),
                item.expectedMin(),
                item.expectedMax(),
                severity,
                detectedAt
        );
    }
}