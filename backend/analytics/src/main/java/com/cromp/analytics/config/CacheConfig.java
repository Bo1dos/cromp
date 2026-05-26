package com.cromp.analytics.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Конфигурация кэша на базе Caffeine (in-memory).
 *
 * <p><b>Переезд на Redis:</b> заменить этот бин на
 * {@code RedisCacheManager} из {@code spring-boot-starter-data-redis}.
 * Аннотации {@code @Cacheable} в сервисах не трогаются.
 *
 * <p>Три кэша с одинаковым TTL:
 * <ul>
 *   <li>{@code analytics.summary}    — сводная статистика из MV
 *   <li>{@code analytics.predictions} — прогнозы от ML
 *   <li>{@code analytics.anomalies}  — аномалии от ML
 * </ul>
 *
 * После {@code REFRESH MATERIALIZED VIEW} все кэши инвалидируются
 * явно через {@link com.cromp.analytics.application.service.MaterializedViewService}.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${analytics.cache.ttl-minutes:60}")
    private long ttlMinutes;

    @Value("${analytics.cache.max-size:500}")
    private long maxSize;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(
                "analytics.summary",
                "analytics.predictions",
                "analytics.anomalies"
        );

        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .maximumSize(maxSize)
                .recordStats()  // статистика hit/miss доступна через Actuator
        );

        return manager;
    }
}