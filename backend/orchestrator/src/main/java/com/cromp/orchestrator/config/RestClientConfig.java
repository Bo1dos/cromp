package com.cromp.orchestrator.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

/**
 * Конфигурация RestClient для выполнения HTTP-задач.
 *
 * Используем Apache HttpClient 5 (httpclient5) под капотом:
 * — поддерживает connection pool;
 * — позволяет задать connect/response таймауты на уровне клиента.
 */
@Configuration
public class RestClientConfig {

    private final OrchestratorProperties properties;

    public RestClientConfig(OrchestratorProperties properties) {
        this.properties = properties;
    }

    @Bean
    public RestClient orchestratorRestClient() {
        long timeoutMs = properties.getExecutor().getHttpTimeoutMs();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(timeoutMs, TimeUnit.MILLISECONDS))
                .setResponseTimeout(Timeout.of(timeoutMs, TimeUnit.MILLISECONDS))
                .build();

        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(200)
                .setMaxConnPerRoute(20)
                .build();

        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}