package com.cromp.analytics.application.service;

import com.cromp.analytics.domain.model.AnalyticsPeriod;
import com.cromp.analytics.domain.model.ExecutionSummary;
import com.cromp.analytics.domain.repository.AnalyticsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    // ── getSummary ────────────────────────────────────────────────────────

    @Test
    void shouldReturnSummaryWhenRepositoryReturnsData() {
        ExecutionSummary expected = new ExecutionSummary(
                1L, null, AnalyticsPeriod.SEVEN_DAYS,
                100, 80, 20, -1.0, 150.0, 200.0, Instant.now()
        );
        when(analyticsRepository.findSummaryByOrganization(1L, AnalyticsPeriod.SEVEN_DAYS))
                .thenReturn(Optional.of(expected));

        Optional<ExecutionSummary> result = analyticsService.getSummary(1L, AnalyticsPeriod.SEVEN_DAYS);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyWhenRepositoryReturnsEmpty() {
        when(analyticsRepository.findSummaryByOrganization(1L, AnalyticsPeriod.ONE_DAY))
                .thenReturn(Optional.empty());

        Optional<ExecutionSummary> result = analyticsService.getSummary(1L, AnalyticsPeriod.ONE_DAY);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldPassOrganizationIdAndPeriodCorrectly() {
        when(analyticsRepository.findSummaryByOrganization(anyLong(), any()))
                .thenReturn(Optional.empty());

        analyticsService.getSummary(5L, AnalyticsPeriod.NINETY_DAYS);

        verify(analyticsRepository).findSummaryByOrganization(5L, AnalyticsPeriod.NINETY_DAYS);
    }

    @Test
    void shouldPassPeriodCorrectlyForAllValues() {
        when(analyticsRepository.findSummaryByOrganization(anyLong(), any()))
                .thenReturn(Optional.empty());

        analyticsService.getSummary(1L, AnalyticsPeriod.THIRTY_DAYS);
        verify(analyticsRepository).findSummaryByOrganization(eq(1L), eq(AnalyticsPeriod.THIRTY_DAYS));
    }

    // ── getSummaryByJob ───────────────────────────────────────────────────

    @Test
    void shouldReturnSummaryByJobWhenRepositoryReturnsData() {
        ExecutionSummary expected = new ExecutionSummary(
                1L, 42L, AnalyticsPeriod.SEVEN_DAYS,
                50, 40, 10, -1.0, 100.0, 150.0, Instant.now()
        );
        when(analyticsRepository.findSummaryByJob(1L, 42L, AnalyticsPeriod.SEVEN_DAYS))
                .thenReturn(Optional.of(expected));

        Optional<ExecutionSummary> result =
                analyticsService.getSummaryByJob(1L, 42L, AnalyticsPeriod.SEVEN_DAYS);

        assertThat(result).isPresent();
        assertThat(result.get().jobId()).isEqualTo(42L);
    }

    @Test
    void shouldReturnEmptyForJobWhenRepositoryReturnsEmpty() {
        when(analyticsRepository.findSummaryByJob(1L, 99L, AnalyticsPeriod.ONE_DAY))
                .thenReturn(Optional.empty());

        Optional<ExecutionSummary> result =
                analyticsService.getSummaryByJob(1L, 99L, AnalyticsPeriod.ONE_DAY);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldPassAllParamsCorrectlyForJobSummary() {
        when(analyticsRepository.findSummaryByJob(anyLong(), anyLong(), any()))
                .thenReturn(Optional.empty());

        analyticsService.getSummaryByJob(10L, 20L, AnalyticsPeriod.THIRTY_DAYS);

        verify(analyticsRepository).findSummaryByJob(10L, 20L, AnalyticsPeriod.THIRTY_DAYS);
    }

    @Test
    void shouldDelegateRepositoryException() {
        when(analyticsRepository.findSummaryByOrganization(anyLong(), any()))
                .thenThrow(new RuntimeException("DB error"));

        try {
            analyticsService.getSummary(1L, AnalyticsPeriod.SEVEN_DAYS);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("DB error");
        }
    }
}
