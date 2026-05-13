package com.cromp.analytics.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalyticsApplicationServiceTest {
    @Test
    void anomaliesReturnsHighSeverityWhenSuccessRateIsLow() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        AnalyticsApplicationService service = new AnalyticsApplicationService(jdbcTemplate);

        when(jdbcTemplate.queryForObject(anyString(), any(org.springframework.jdbc.core.RowMapper.class), any(Object[].class)))
                .thenAnswer(invocation -> {
                    org.springframework.jdbc.core.RowMapper<?> mapper = invocation.getArgument(1);
                    java.sql.ResultSet rs = mock(java.sql.ResultSet.class);
                    when(rs.getLong("total")).thenReturn(10L);
                    when(rs.getLong("succeeded")).thenReturn(4L);
                    when(rs.getLong("failed")).thenReturn(6L);
                    when(rs.getLong("cancelled")).thenReturn(0L);
                    when(rs.getLong("skipped")).thenReturn(0L);
                    when(rs.getDouble(anyString())).thenReturn(0.0);
                    when(rs.wasNull()).thenReturn(true);
                    return mapper.mapRow(rs, 0);
                });

        var anomalies = service.anomalies(1L, 2L, Instant.now().minusSeconds(3600), Instant.now(), null);

        assertThat(anomalies).hasSize(1);
        assertThat(anomalies.getFirst().severity()).isEqualTo("HIGH");
    }
}
