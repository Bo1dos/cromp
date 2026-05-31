package com.cromp.analytics.infrastructure.persistence.repository;

import com.cromp.analytics.domain.model.AnalyticsPeriod;
import com.cromp.analytics.domain.model.ExecutionSummary;
import com.cromp.analytics.domain.repository.AnalyticsRepository.DailyExecutionRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsRepositoryImplTest {

    @Mock
    private JdbcTemplate jdbc;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private AnalyticsRepositoryImpl repository;

    // ── findSummaryByOrganization ─────────────────────────────────────────

    @Test
    void shouldReturnSummaryWhenQueryReturnsRows() throws SQLException {
        when(resultSet.getLong("total")).thenReturn(100L);
        when(resultSet.getLong("succeeded")).thenReturn(80L);
        when(resultSet.getLong("failed")).thenReturn(20L);
        when(resultSet.getDouble("avg_duration_ms")).thenReturn(150.0);
        when(resultSet.wasNull()).thenReturn(false, false);
        when(resultSet.getDouble("p95_duration_ms")).thenReturn(200.0);

        when(jdbc.queryForObject(anyString(), any(RowMapper.class), eq(1L), eq(7)))
                .thenAnswer(invocation -> {
                    RowMapper<ExecutionSummary> mapper = invocation.getArgument(1);
                    return mapper.mapRow(resultSet, 0);
                });

        Optional<ExecutionSummary> result =
                repository.findSummaryByOrganization(1L, AnalyticsPeriod.SEVEN_DAYS);

        assertThat(result).isPresent();
        assertThat(result.get().total()).isEqualTo(100);
        assertThat(result.get().succeeded()).isEqualTo(80);
        assertThat(result.get().failed()).isEqualTo(20);
    }

    @Test
    void shouldReturnEmptyWhenTotalIsZero() throws SQLException {
        when(resultSet.getLong("total")).thenReturn(0L);

        when(jdbc.queryForObject(anyString(), any(RowMapper.class), eq(1L), eq(7)))
                .thenAnswer(invocation -> {
                    RowMapper<ExecutionSummary> mapper = invocation.getArgument(1);
                    return mapper.mapRow(resultSet, 0);
                });

        Optional<ExecutionSummary> result =
                repository.findSummaryByOrganization(1L, AnalyticsPeriod.SEVEN_DAYS);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenQueryThrowsException() {
        when(jdbc.queryForObject(anyString(), any(RowMapper.class), eq(1L), eq(7)))
                .thenThrow(new RuntimeException("DB error"));

        Optional<ExecutionSummary> result =
                repository.findSummaryByOrganization(1L, AnalyticsPeriod.SEVEN_DAYS);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldSetOrganizationIdAndPeriod() throws SQLException {
        when(resultSet.getLong("total")).thenReturn(100L);
        when(resultSet.getLong("succeeded")).thenReturn(80L);
        when(resultSet.getLong("failed")).thenReturn(20L);
        when(resultSet.getDouble("avg_duration_ms")).thenReturn(150.0);
        when(resultSet.wasNull()).thenReturn(false, false);
        when(resultSet.getDouble("p95_duration_ms")).thenReturn(200.0);

        when(jdbc.queryForObject(anyString(), any(RowMapper.class), eq(5L), eq(30)))
                .thenAnswer(invocation -> {
                    RowMapper<ExecutionSummary> mapper = invocation.getArgument(1);
                    return mapper.mapRow(resultSet, 0);
                });

        Optional<ExecutionSummary> result =
                repository.findSummaryByOrganization(5L, AnalyticsPeriod.THIRTY_DAYS);

        assertThat(result).isPresent();
        assertThat(result.get().organizationId()).isEqualTo(5L);
        assertThat(result.get().period()).isEqualTo(AnalyticsPeriod.THIRTY_DAYS);
        assertThat(result.get().jobId()).isNull();
    }

    @Test
    void shouldHandleNullDurations() throws SQLException {
        when(resultSet.getLong("total")).thenReturn(100L);
        when(resultSet.getLong("succeeded")).thenReturn(80L);
        when(resultSet.getLong("failed")).thenReturn(20L);
        when(resultSet.getDouble("avg_duration_ms")).thenReturn(0.0);
        when(resultSet.wasNull()).thenReturn(true, true);
        when(resultSet.getDouble("p95_duration_ms")).thenReturn(0.0);

        when(jdbc.queryForObject(anyString(), any(RowMapper.class), eq(1L), eq(7)))
                .thenAnswer(invocation -> {
                    RowMapper<ExecutionSummary> mapper = invocation.getArgument(1);
                    return mapper.mapRow(resultSet, 0);
                });

        Optional<ExecutionSummary> result =
                repository.findSummaryByOrganization(1L, AnalyticsPeriod.SEVEN_DAYS);

        assertThat(result).isPresent();
        assertThat(result.get().avgDurationMs()).isNull();
        assertThat(result.get().p95DurationMs()).isNull();
    }

    // ── findSummaryByJob ──────────────────────────────────────────────────

    @Test
    void shouldReturnJobSummaryWithCorrectParams() throws SQLException {
        when(resultSet.getLong("total")).thenReturn(50L);
        when(resultSet.getLong("succeeded")).thenReturn(45L);
        when(resultSet.getLong("failed")).thenReturn(5L);
        when(resultSet.getDouble("avg_duration_ms")).thenReturn(100.0);
        when(resultSet.wasNull()).thenReturn(false, false);
        when(resultSet.getDouble("p95_duration_ms")).thenReturn(150.0);

        when(jdbc.queryForObject(anyString(), any(RowMapper.class), eq(1L), eq(42L), eq(7)))
                .thenAnswer(invocation -> {
                    RowMapper<ExecutionSummary> mapper = invocation.getArgument(1);
                    return mapper.mapRow(resultSet, 0);
                });

        Optional<ExecutionSummary> result =
                repository.findSummaryByJob(1L, 42L, AnalyticsPeriod.SEVEN_DAYS);

        assertThat(result).isPresent();
        assertThat(result.get().jobId()).isEqualTo(42L);
        assertThat(result.get().total()).isEqualTo(50);
    }

    @Test
    void shouldReturnEmptyForJobWhenTotalIsZero() throws SQLException {
        when(resultSet.getLong("total")).thenReturn(0L);

        when(jdbc.queryForObject(anyString(), any(RowMapper.class), eq(1L), eq(42L), eq(1)))
                .thenAnswer(invocation -> {
                    RowMapper<ExecutionSummary> mapper = invocation.getArgument(1);
                    return mapper.mapRow(resultSet, 0);
                });

        Optional<ExecutionSummary> result =
                repository.findSummaryByJob(1L, 42L, AnalyticsPeriod.ONE_DAY);

        assertThat(result).isEmpty();
    }

    // ── findDailyRows ─────────────────────────────────────────────────────

    @Test
    void shouldReturnDailyRowsForJob() {
        DailyExecutionRow row = new DailyExecutionRow(1L, "2025-06-01", 10, 9, 1, 100.0, 150.0);
        when(jdbc.query(anyString(), any(RowMapper.class), eq(1L), eq(42L), eq(90)))
                .thenReturn(List.of(row));

        List<DailyExecutionRow> result = repository.findDailyRows(1L, 42L, 90);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).jobId()).isEqualTo(1L);
        assertThat(result.get(0).day()).isEqualTo("2025-06-01");
    }

    @Test
    void shouldReturnDailyRowsForOrganization() {
        DailyExecutionRow row1 = new DailyExecutionRow(1L, "2025-06-02", 10, 9, 1, 100.0, 150.0);
        DailyExecutionRow row2 = new DailyExecutionRow(2L, "2025-06-02", 20, 18, 2, 200.0, 250.0);
        when(jdbc.query(anyString(), any(RowMapper.class), eq(1L), eq(90)))
                .thenReturn(List.of(row1, row2));

        List<DailyExecutionRow> result = repository.findDailyRows(1L, null, 90);

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListWhenNoRowsFound() {
        when(jdbc.query(anyString(), any(RowMapper.class), eq(1L), eq(42L), eq(90)))
                .thenReturn(List.of());

        List<DailyExecutionRow> result = repository.findDailyRows(1L, 42L, 90);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldRouteToOrganizationQueryWhenJobIdIsNull() {
        when(jdbc.query(anyString(), any(RowMapper.class), eq(1L), eq(90)))
                .thenReturn(List.of());

        repository.findDailyRows(1L, null, 90);

        // Verify the org-level query was used (2 params: orgId, days)
        verify(jdbc).query(anyString(), any(RowMapper.class), eq(1L), eq(90));
    }

    @Test
    void shouldRouteToJobQueryWhenJobIdIsNotNull() {
        when(jdbc.query(anyString(), any(RowMapper.class), eq(1L), eq(42L), eq(90)))
                .thenReturn(List.of());

        repository.findDailyRows(1L, 42L, 90);

        // Verify the job-level query was used (3 params: orgId, jobId, days)
        verify(jdbc).query(anyString(), any(RowMapper.class), eq(1L), eq(42L), eq(90));
    }
}
