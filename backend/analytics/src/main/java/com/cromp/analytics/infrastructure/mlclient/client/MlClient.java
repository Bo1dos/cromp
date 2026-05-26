package com.cromp.analytics.infrastructure.mlclient.client;

import com.cromp.analytics.infrastructure.mlclient.contract.MlContracts;

/**
 * Порт для взаимодействия с Python ML-сервисом.
 *
 * Реализация ({@link MlClientImpl}) использует RestClient.
 * При недоступности сервиса реализация возвращает деградированный ответ
 * (пустые списки) — не бросает исключений наружу.
 */
public interface MlClient {

    /**
     * Запрашивает прогнозы для задачи (или всех задач организации).
     * При ошибке сети / таймауте возвращает ответ с пустым списком.
     */
    MlContracts.PredictionResponse getPredictions(MlContracts.PredictionRequest request);

    /**
     * Запрашивает обнаружение аномалий за указанный период.
     * При ошибке сети / таймауте возвращает ответ с пустым списком.
     */
    MlContracts.AnomalyResponse getAnomalies(MlContracts.AnomalyRequest request);
}