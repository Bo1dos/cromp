package com.cromp.analytics.infrastructure.mlclient.client;

import com.cromp.analytics.infrastructure.mlclient.contract.MlContracts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Реализация {@link MlClient} через Spring RestClient.
 *
 * <p><b>Fallback-стратегия:</b> при любой ошибке сети, таймауте или
 * недоступности ML-сервиса логируем предупреждение и возвращаем
 * деградированный ответ (пустые списки). Верхний слой не падает.
 *
 * <p>Таймауты настраиваются в {@code MlClientConfig} на уровне HttpClient.
 */
@Slf4j
@Component
public class MlClientImpl implements MlClient {

    private static final String PREDICTIONS_PATH = "/predictions";
    private static final String ANOMALIES_PATH   = "/anomalies";

    private static final MlContracts.PredictionResponse EMPTY_PREDICTIONS =
            new MlContracts.PredictionResponse(List.of());

    private static final MlContracts.AnomalyResponse EMPTY_ANOMALIES =
            new MlContracts.AnomalyResponse(List.of());

    private final RestClient restClient;

    public MlClientImpl(@Qualifier("mlRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public MlContracts.PredictionResponse getPredictions(MlContracts.PredictionRequest request) {
        try {
            MlContracts.PredictionResponse response = restClient.post()
                    .uri(PREDICTIONS_PATH)
                    .body(request)
                    .retrieve()
                    .body(MlContracts.PredictionResponse.class);

            if (response == null || response.predictions() == null) {
                log.warn("[ml-client] predictions response is null or empty, returning fallback");
                return EMPTY_PREDICTIONS;
            }

            log.debug("[ml-client] received {} prediction(s) for orgId={} jobId={}",
                    response.predictions().size(), request.orgId(), request.jobId());
            return response;

        } catch (ResourceAccessException e) {
            log.warn("[ml-client] ML service unavailable for predictions orgId={}: {}",
                    request.orgId(), e.getMessage());
            return EMPTY_PREDICTIONS;

        } catch (Exception e) {
            log.error("[ml-client] unexpected error calling predictions orgId={}",
                    request.orgId(), e);
            return EMPTY_PREDICTIONS;
        }
    }

    @Override
    public MlContracts.AnomalyResponse getAnomalies(MlContracts.AnomalyRequest request) {
        try {
            MlContracts.AnomalyResponse response = restClient.post()
                    .uri(ANOMALIES_PATH)
                    .body(request)
                    .retrieve()
                    .body(MlContracts.AnomalyResponse.class);

            if (response == null || response.anomalies() == null) {
                log.warn("[ml-client] anomalies response is null or empty, returning fallback");
                return EMPTY_ANOMALIES;
            }

            log.debug("[ml-client] received {} anomal(ies) for orgId={} jobId={}",
                    response.anomalies().size(), request.orgId(), request.jobId());
            return response;

        } catch (ResourceAccessException e) {
            log.warn("[ml-client] ML service unavailable for anomalies orgId={}: {}",
                    request.orgId(), e.getMessage());
            return EMPTY_ANOMALIES;

        } catch (Exception e) {
            log.error("[ml-client] unexpected error calling anomalies orgId={}",
                    request.orgId(), e);
            return EMPTY_ANOMALIES;
        }
    }
}