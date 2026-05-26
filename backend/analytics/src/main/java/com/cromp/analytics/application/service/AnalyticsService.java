package com.cromp.analytics.application.service;

import com.cromp.analytics.domain.model.AnalyticsPeriod;
import com.cromp.analytics.domain.model.ExecutionSummary;
import com.cromp.analytics.domain.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Сервис сводной статистики выполнений.
 *
 * Читает агрегаты из {@code mv_job_execution_daily} через {@link AnalyticsRepository}.
 * Результаты кэшируются — TTL настраивается в {@code CacheConfig}.
 *
 * Кэш-ключ включает все параметры запроса: при смене periodа или jobId
 * кэш не пересекается.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    /**
     * Возвращает сводку по всем задачам организации за период.
     *
     * @param organizationId идентификатор организации
     * @param period         период агрегации
     * @return Optional.empty() если данных нет
     */
    @Cacheable(
            cacheNames = "analytics.summary",
            key = "#organizationId + ':' + #period.name()"
    )
    public Optional<ExecutionSummary> getSummary(Long organizationId, AnalyticsPeriod period) {
        log.debug("[analytics] querying summary orgId={} period={}", organizationId, period);
        return analyticsRepository.findSummaryByOrganization(organizationId, period);
    }

    /**
     * Возвращает сводку по конкретной задаче за период.
     *
     * @param organizationId идентификатор организации
     * @param jobId          идентификатор задачи
     * @param period         период агрегации
     * @return Optional.empty() если данных нет
     */
    @Cacheable(
            cacheNames = "analytics.summary",
            key = "#organizationId + ':' + #jobId + ':' + #period.name()"
    )
    public Optional<ExecutionSummary> getSummaryByJob(Long organizationId, Long jobId,
                                                       AnalyticsPeriod period) {
        log.debug("[analytics] querying summary orgId={} jobId={} period={}",
                organizationId, jobId, period);
        return analyticsRepository.findSummaryByJob(organizationId, jobId, period);
    }
}