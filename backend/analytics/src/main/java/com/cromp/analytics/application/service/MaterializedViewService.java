package com.cromp.analytics.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Периодическое обновление материализованного представления
 * {@code mv_job_execution_daily} и инвалидация связанных кэшей.
 *
 * <p>CONCURRENT REFRESH позволяет читать из MV во время обновления —
 * запросы не блокируются. Требует уникального индекса на MV
 * ({@code idx_mv_job_exec_daily}), который создан в Liquibase changelog.
 *
 * <p>После обновления инвалидируем кэши summary, predictions и anomalies —
 * иначе пользователи будут видеть устаревшие данные до истечения TTL.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterializedViewService {

    private static final String REFRESH_SQL =
            "REFRESH MATERIALIZED VIEW CONCURRENTLY mv_job_execution_daily";

    private final JdbcTemplate jdbc;
    private final CacheManager cacheManager;

    @Scheduled(fixedRateString = "${analytics.mv.refresh-interval-ms:3600000}")
    public void refresh() {
        log.info("[mv-refresh] starting REFRESH MATERIALIZED VIEW mv_job_execution_daily");
        long start = System.currentTimeMillis();

        try {
            jdbc.execute(REFRESH_SQL);
            long elapsed = System.currentTimeMillis() - start;
            log.info("[mv-refresh] completed in {}ms", elapsed);

            // Инвалидируем кэши — данные обновились, кэш устарел
            evictCaches();

        } catch (Exception e) {
            log.error("[mv-refresh] failed to refresh materialized view", e);
            // Не бросаем — следующий цикл попробует снова
        }
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void evictCaches() {
        evict("analytics.summary");
        evict("analytics.predictions");
        evict("analytics.anomalies");
    }

    private void evict(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.debug("[mv-refresh] evicted cache '{}'", cacheName);
        }
    }
}