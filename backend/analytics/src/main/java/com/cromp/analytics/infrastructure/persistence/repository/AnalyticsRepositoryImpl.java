package com.cromp.analytics.infrastructure.persistence.repository;

import com.cromp.analytics.domain.model.AnalyticsPeriod;
import com.cromp.analytics.domain.model.ExecutionSummary;
import com.cromp.analytics.domain.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Реализация {@link AnalyticsRepository} через JdbcTemplate.
 *
 * Все запросы идут к материализованному представлению {@code mv_job_execution_daily}.
 * JPA здесь не нужен — нет сущностей, только агрегации.
 *
 * Фильтрация по периоду реализована через {@code day >= now() - INTERVAL '? days'},
 * где {@code ?} — число дней из {@link AnalyticsPeriod#getDays()}.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AnalyticsRepositoryImpl implements AnalyticsRepository {

    private final JdbcTemplate jdbc;

    // ── Сводная статистика ────────────────────────────────────────────────────

    @Override
    public Optional<ExecutionSummary> findSummaryByOrganization(Long organizationId,
                                                                  AnalyticsPeriod period) {
        String sql = """
                SELECT
                    SUM(total_executions)                           AS total,
                    SUM(succeeded)                                  AS succeeded,
                    SUM(failed)                                     AS failed,
                    AVG(avg_attempt_duration_ms)                    AS avg_duration_ms,
                    percentile_cont(0.95) WITHIN GROUP
                        (ORDER BY p95_duration_ms)                  AS p95_duration_ms
                FROM mv_job_execution_daily
                WHERE organization_id = ?
                  AND day >= now() - (? * INTERVAL '1 day')
                """;

        return queryForSummary(sql, organizationId, null, period,
                organizationId, period.getDays());
    }

    @Override
    public Optional<ExecutionSummary> findSummaryByJob(Long organizationId, Long jobId,
                                                        AnalyticsPeriod period) {
        String sql = """
                SELECT
                    SUM(total_executions)                           AS total,
                    SUM(succeeded)                                  AS succeeded,
                    SUM(failed)                                     AS failed,
                    AVG(avg_attempt_duration_ms)                    AS avg_duration_ms,
                    percentile_cont(0.95) WITHIN GROUP
                        (ORDER BY p95_duration_ms)                  AS p95_duration_ms
                FROM mv_job_execution_daily
                WHERE organization_id = ?
                  AND job_id = ?
                  AND day >= now() - (? * INTERVAL '1 day')
                """;

        return queryForSummary(sql, organizationId, jobId, period,
                organizationId, jobId, period.getDays());
    }

    // ── Исторические строки для ML ────────────────────────────────────────────

    @Override
    public List<DailyExecutionRow> findDailyRows(Long organizationId, Long jobId, int days) {
        if (jobId != null) {
            return findDailyRowsByJob(organizationId, jobId, days);
        }
        return findDailyRowsByOrganization(organizationId, days);
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private Optional<ExecutionSummary> queryForSummary(String sql,
                                                        Long organizationId,
                                                        Long jobId,
                                                        AnalyticsPeriod period,
                                                        Object... params) {
        try {
            ExecutionSummary result = jdbc.queryForObject(sql, (rs, rowNum) -> {
                long total     = rs.getLong("total");
                long succeeded = rs.getLong("succeeded");
                long failed    = rs.getLong("failed");

                Double avgDuration = nullableDouble(rs, "avg_duration_ms");
                Double p95Duration = nullableDouble(rs, "p95_duration_ms");

                // Если total = 0 — данных нет, возвращаем null → Optional.empty()
                if (total == 0) return null;

                return new ExecutionSummary(
                        organizationId,
                        jobId,
                        period,
                        total,
                        succeeded,
                        failed,
                        0.0,        // errorRate вычислится в compact constructor
                        avgDuration,
                        p95Duration,
                        Instant.now()
                );
            }, params);

            return Optional.ofNullable(result);

        } catch (Exception e) {
            log.error("Failed to query execution summary orgId={} jobId={} period={}",
                    organizationId, jobId, period, e);
            return Optional.empty();
        }
    }

    private List<DailyExecutionRow> findDailyRowsByJob(Long organizationId,
                                                        Long jobId,
                                                        int days) {
        String sql = """
                SELECT
                    job_id,
                    to_char(day, 'YYYY-MM-DD')  AS day,
                    total_executions            AS total,
                    succeeded,
                    failed,
                    avg_attempt_duration_ms     AS avg_duration_ms,
                    p95_duration_ms
                FROM mv_job_execution_daily
                WHERE organization_id = ?
                  AND job_id = ?
                  AND day >= now() - (? * INTERVAL '1 day')
                ORDER BY day DESC
                """;

        return jdbc.query(sql, dailyRowMapper(), organizationId, jobId, days);
    }

    private List<DailyExecutionRow> findDailyRowsByOrganization(Long organizationId,
                                                                  int days) {
        String sql = """
                SELECT
                    job_id,
                    to_char(day, 'YYYY-MM-DD')  AS day,
                    total_executions            AS total,
                    succeeded,
                    failed,
                    avg_attempt_duration_ms     AS avg_duration_ms,
                    p95_duration_ms
                FROM mv_job_execution_daily
                WHERE organization_id = ?
                  AND day >= now() - (? * INTERVAL '1 day')
                ORDER BY job_id, day DESC
                """;

        return jdbc.query(sql, dailyRowMapper(), organizationId, days);
    }

    private RowMapper<DailyExecutionRow> dailyRowMapper() {
        return (rs, rowNum) -> new DailyExecutionRow(
                rs.getLong("job_id"),
                rs.getString("day"),
                rs.getLong("total"),
                rs.getLong("succeeded"),
                rs.getLong("failed"),
                nullableDouble(rs, "avg_duration_ms"),
                nullableDouble(rs, "p95_duration_ms")
        );
    }

    /**
     * Возвращает null вместо 0.0 для nullable double колонок.
     * JDBC возвращает 0.0 для NULL при getDouble() — это маскирует отсутствие данных.
     */
    private Double nullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }
}