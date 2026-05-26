package com.cromp.analytics.domain.repository;

import com.cromp.analytics.domain.model.AnalyticsPeriod;
import com.cromp.analytics.domain.model.ExecutionSummary;

import java.util.List;
import java.util.Optional;

/**
 * Доменный порт репозитория аналитики.
 * Реализуется через JdbcTemplate в infrastructure-слое.
 *
 * Все методы читают из материализованного представления
 * {@code mv_job_execution_daily} — read-only, без side effects.
 */
public interface AnalyticsRepository {

    /**
     * Сводная статистика по всем задачам организации за период.
     */
    Optional<ExecutionSummary> findSummaryByOrganization(Long organizationId,
                                                          AnalyticsPeriod period);

    /**
     * Сводная статистика по конкретной задаче за период.
     */
    Optional<ExecutionSummary> findSummaryByJob(Long organizationId, Long jobId,
                                                 AnalyticsPeriod period);

    /**
     * Исторические данные выполнений для ML-сервиса.
     * Возвращает сырые строки: jobId, day, total, succeeded, avgDurationMs и т.д.
     * Используется для формирования запроса к Python.
     *
     * @param days глубина истории в днях
     */
    List<DailyExecutionRow> findDailyRows(Long organizationId, Long jobId, int days);

    /**
     * Проекция одной строки mv_job_execution_daily.
     * Используется при передаче данных в ML-сервис.
     */
    record DailyExecutionRow(
            Long jobId,
            String day,           // ISO date string
            long total,
            long succeeded,
            long failed,
            Double avgDurationMs,
            Double p95DurationMs
    ) {}
}