package com.cromp.analytics.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Периодическое обновление таблицы агрегатов выполнений
 * {@code mv_job_execution_daily} и инвалидация связанных кэшей.
 *
 * <p>Таблица наполняется из {@code executions} + {@code execution_attempts}
 * через {@code INSERT ... ON CONFLICT DO UPDATE} — существующие строки
 * обновляются свежими агрегатами, новые добавляются.
 *
 * <p>После обновления инвалидируем кэши summary, predictions и anomalies —
 * иначе пользователи будут видеть устаревшие данные до истечения TTL.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterializedViewService {

    private static final String REFRESH_SQL = """
            INSERT INTO mv_job_execution_daily (
                job_id, organization_id, day,
                total_executions, succeeded, failed,
                avg_attempt_duration_ms, p50_duration_ms,
                p95_duration_ms, max_duration_ms, total_attempts
            )
            SELECT
                e.job_id,
                e.organization_id,
                date_trunc('day', e.created_at)::date AS day,
                count(*) AS total_executions,
                count(*) FILTER (WHERE e.final_status = 'SUCCEEDED') AS succeeded,
                count(*) FILTER (WHERE e.final_status = 'FAILED') AS failed,
                avg(a.duration_ms) FILTER (WHERE a.duration_ms IS NOT NULL) AS avg_attempt_duration_ms,
                percentile_cont(0.5) WITHIN GROUP (ORDER BY a.duration_ms)
                    FILTER (WHERE a.duration_ms IS NOT NULL) AS p50_duration_ms,
                percentile_cont(0.95) WITHIN GROUP (ORDER BY a.duration_ms)
                    FILTER (WHERE a.duration_ms IS NOT NULL) AS p95_duration_ms,
                max(a.duration_ms) FILTER (WHERE a.duration_ms IS NOT NULL) AS max_duration_ms,
                sum(e.total_attempts) AS total_attempts
            FROM executions e
            LEFT JOIN execution_attempts a ON a.execution_id = e.id
            WHERE e.created_at >= now() - INTERVAL '90 days'
            GROUP BY e.job_id, e.organization_id, date_trunc('day', e.created_at)
            ON CONFLICT (job_id, day) DO UPDATE SET
                organization_id         = EXCLUDED.organization_id,
                total_executions        = EXCLUDED.total_executions,
                succeeded               = EXCLUDED.succeeded,
                failed                  = EXCLUDED.failed,
                avg_attempt_duration_ms = EXCLUDED.avg_attempt_duration_ms,
                p50_duration_ms         = EXCLUDED.p50_duration_ms,
                p95_duration_ms         = EXCLUDED.p95_duration_ms,
                max_duration_ms         = EXCLUDED.max_duration_ms,
                total_attempts          = EXCLUDED.total_attempts
            """;

    private final JdbcTemplate jdbc;
    private final CacheManager cacheManager;

    @Scheduled(fixedRateString = "${analytics.mv.refresh-interval-ms:3600000}")
    public void refresh() {
        log.info("[mv-refresh] starting upsert into mv_job_execution_daily");
        long start = System.currentTimeMillis();

        try {
            int updated = jdbc.update(REFRESH_SQL);
            long elapsed = System.currentTimeMillis() - start;
            log.info("[mv-refresh] completed in {}ms, {} rows affected", elapsed, updated);

            // Инвалидируем кэши — данные обновились, кэш устарел
            evictCaches();

        } catch (Exception e) {
            log.error("[mv-refresh] failed to refresh analytics table", e);
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