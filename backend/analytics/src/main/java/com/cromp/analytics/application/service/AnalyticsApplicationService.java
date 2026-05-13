package com.cromp.analytics.application.service;

import com.cromp.analytics.api.dto.response.AnomalyResponse;
import com.cromp.analytics.api.dto.response.DailyExecutionStatsResponse;
import com.cromp.analytics.api.dto.response.ExecutionSummaryResponse;
import com.cromp.analytics.api.dto.response.PredictionResponse;
import com.cromp.analytics.api.service.AnalyticsFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsApplicationService implements AnalyticsFacade {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public ExecutionSummaryResponse executionSummary(Long organizationId, Long jobId, Instant from, Instant to) {
        Instant rangeFrom = from != null ? from : Instant.now().minusSeconds(30L * 24 * 60 * 60);
        Instant rangeTo = to != null ? to : Instant.now();
        StringBuilder sql = new StringBuilder("""
                SELECT
                  COUNT(*) AS total,
                  COUNT(*) FILTER (WHERE e.final_status = 'SUCCEEDED') AS succeeded,
                  COUNT(*) FILTER (WHERE e.final_status = 'FAILED') AS failed,
                  COUNT(*) FILTER (WHERE e.final_status = 'CANCELLED') AS cancelled,
                  COUNT(*) FILTER (WHERE e.final_status = 'SKIPPED') AS skipped,
                  AVG(a.duration_ms) FILTER (WHERE a.duration_ms IS NOT NULL) AS avg_duration,
                  percentile_cont(0.95) WITHIN GROUP (ORDER BY a.duration_ms)
                    FILTER (WHERE a.duration_ms IS NOT NULL) AS p95_duration
                FROM executions e
                LEFT JOIN execution_attempts a ON a.execution_id = e.id
                WHERE e.organization_id = ? AND e.created_at >= ? AND e.created_at <= ?
                """);
        List<Object> args = new ArrayList<>(List.of(organizationId, Timestamp.from(rangeFrom), Timestamp.from(rangeTo)));
        if (jobId != null) {
            sql.append(" AND e.job_id = ?");
            args.add(jobId);
        }
        return jdbcTemplate.queryForObject(sql.toString(), (rs, rowNum) -> mapSummary(rs, organizationId, jobId, rangeFrom, rangeTo), args.toArray());
    }

    @Override
    public List<DailyExecutionStatsResponse> dailyStats(Long organizationId, Long jobId) {
        StringBuilder sql = new StringBuilder("""
                SELECT job_id, organization_id, day, total_executions, succeeded, failed,
                       avg_attempt_duration_ms, p50_duration_ms, p95_duration_ms,
                       max_duration_ms, total_attempts
                FROM mv_job_execution_daily
                WHERE organization_id = ?
                """);
        List<Object> args = new ArrayList<>(List.of(organizationId));
        if (jobId != null) {
            sql.append(" AND job_id = ?");
            args.add(jobId);
        }
        sql.append(" ORDER BY day DESC, job_id");
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> new DailyExecutionStatsResponse(
                rs.getLong("job_id"),
                rs.getLong("organization_id"),
                rs.getTimestamp("day").toLocalDateTime().toLocalDate(),
                rs.getLong("total_executions"),
                rs.getLong("succeeded"),
                rs.getLong("failed"),
                nullableDouble(rs, "avg_attempt_duration_ms"),
                nullableDouble(rs, "p50_duration_ms"),
                nullableDouble(rs, "p95_duration_ms"),
                (Integer) rs.getObject("max_duration_ms"),
                rs.getLong("total_attempts")
        ), args.toArray());
    }

    @Override
    public List<PredictionResponse> predictions(Long organizationId, Long jobId) {
        return dailyStats(organizationId, jobId).stream()
                .map(stat -> {
                    double failureProbability = stat.totalExecutions() == 0
                            ? 0.0
                            : (double) stat.failed() / stat.totalExecutions();
                    String recommendation = failureProbability >= 0.3
                            ? "Review retry policy, timeout and target availability"
                            : "No action required";
                    return new PredictionResponse(stat.jobId(), failureProbability,
                            stat.avgAttemptDurationMs(), "LOW", recommendation);
                })
                .toList();
    }

    @Override
    public List<AnomalyResponse> anomalies(Long organizationId, Long jobId, Instant from, Instant to, String severity) {
        ExecutionSummaryResponse summary = executionSummary(organizationId, jobId, from, to);
        if (summary.totalExecutions() == 0 || summary.successRate() >= 0.8) {
            return List.of();
        }
        String resolvedSeverity = summary.successRate() < 0.5 ? "HIGH" : "MEDIUM";
        if (severity != null && !severity.equalsIgnoreCase(resolvedSeverity)) {
            return List.of();
        }
        return List.of(new AnomalyResponse(jobId, null, resolvedSeverity, "LOW_SUCCESS_RATE",
                "Execution success rate is below expected threshold", Instant.now()));
    }

    @Override
    @Transactional
    public void refreshDailyStats() {
        jdbcTemplate.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY mv_job_execution_daily");
    }

    private static ExecutionSummaryResponse mapSummary(ResultSet rs, Long organizationId, Long jobId,
                                                       Instant rangeFrom, Instant rangeTo) throws SQLException {
        long total = rs.getLong("total");
        long succeeded = rs.getLong("succeeded");
        return new ExecutionSummaryResponse(
                organizationId,
                jobId,
                rangeFrom,
                rangeTo,
                total,
                succeeded,
                rs.getLong("failed"),
                rs.getLong("cancelled"),
                rs.getLong("skipped"),
                total == 0 ? 0.0 : (double) succeeded / total,
                nullableDouble(rs, "avg_duration"),
                nullableDouble(rs, "p95_duration")
        );
    }

    private static Double nullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }
}
