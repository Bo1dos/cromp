package com.cromp.analytics.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

/**
 * Конфигурация RestClient для Python ML-сервиса.
 *
 * Base URL берётся из {@code ml.service.url}.
 * Таймауты — из {@code ml.service.timeout-ms}.
 *
 * Переезд на другой HTTP-клиент или URL — только здесь,
 * {@link com.cromp.analytics.infrastructure.mlclient.client.MlClientImpl} не трогается.
 */
@Configuration
public class MlClientConfig {

    @Value("${ml.service.url:http://localhost:8000}")
    private String mlServiceUrl;

    @Value("${ml.service.timeout-ms:10000}")
    private long timeoutMs;

    @Bean(name = "mlRestClient")
    public RestClient mlRestClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(timeoutMs, TimeUnit.MILLISECONDS))
                .setResponseTimeout(Timeout.of(timeoutMs, TimeUnit.MILLISECONDS))
                .build();

        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(
                        PoolingHttpClientConnectionManagerBuilder.create()
                                .setMaxConnTotal(20)
                                .setMaxConnPerRoute(10)
                                .build()
                )
                .build();

        return RestClient.builder()
                .baseUrl(mlServiceUrl)
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }
}