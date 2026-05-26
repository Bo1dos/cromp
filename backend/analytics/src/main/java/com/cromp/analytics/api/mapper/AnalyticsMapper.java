package com.cromp.analytics.api.mapper;

import com.cromp.analytics.api.dto.AnalyticsDtos;
import com.cromp.analytics.domain.model.AnomalyDetection;
import com.cromp.analytics.domain.model.ExecutionSummary;
import com.cromp.analytics.domain.model.Prediction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct-маппер из доменных объектов в API DTO.
 *
 * {@code componentModel = "spring"} — маппер становится Spring-бином,
 * инжектируется через конструктор в контроллер.
 */
@Mapper(componentModel = "spring")
public interface AnalyticsMapper {

    // ── Summary ───────────────────────────────────────────────────────────────

    @Mapping(target = "period", expression = "java(summary.period().name())")
    AnalyticsDtos.SummaryResponse toSummaryResponse(ExecutionSummary summary);

    // ── Predictions ───────────────────────────────────────────────────────────

    AnalyticsDtos.PredictionItem toPredictionItem(Prediction prediction);

    default AnalyticsDtos.PredictionsResponse toPredictionsResponse(
            Long organizationId, Long jobId, List<Prediction> predictions) {
        return new AnalyticsDtos.PredictionsResponse(
                organizationId,
                jobId,
                predictions.stream().map(this::toPredictionItem).toList()
        );
    }

    // ── Anomalies ─────────────────────────────────────────────────────────────

    @Mapping(target = "severity", expression = "java(anomaly.severity().name())")
    AnalyticsDtos.AnomalyItem toAnomalyItem(AnomalyDetection anomaly);

    default AnalyticsDtos.AnomaliesResponse toAnomaliesResponse(
            Long organizationId, Long jobId,
            String from, String to,
            List<AnomalyDetection> anomalies) {
        return new AnalyticsDtos.AnomaliesResponse(
                organizationId,
                jobId,
                from,
                to,
                anomalies.stream().map(this::toAnomalyItem).toList()
        );
    }
}