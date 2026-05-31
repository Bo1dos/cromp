package com.cromp.analytics.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaterializedViewServiceTest {

    @Mock
    private JdbcTemplate jdbc;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache summaryCache;

    @Mock
    private Cache predictionsCache;

    @Mock
    private Cache anomaliesCache;

    @InjectMocks
    private MaterializedViewService materializedViewService;

    // ── refresh ───────────────────────────────────────────────────────────

    @Test
    void shouldExecuteRefreshSqlAndEvictCachesOnSuccess() {
        when(cacheManager.getCache("analytics.summary")).thenReturn(summaryCache);
        when(cacheManager.getCache("analytics.predictions")).thenReturn(predictionsCache);
        when(cacheManager.getCache("analytics.anomalies")).thenReturn(anomaliesCache);

        materializedViewService.refresh();

        verify(jdbc).execute("REFRESH MATERIALIZED VIEW CONCURRENTLY mv_job_execution_daily");
        verify(summaryCache).clear();
        verify(predictionsCache).clear();
        verify(anomaliesCache).clear();
    }

    @Test
    void shouldNotThrowWhenJdbcExecuteFails() {
        doThrow(new RuntimeException("DB connection lost"))
                .when(jdbc).execute(anyString());

        // Should not throw - the method swallows exceptions
        materializedViewService.refresh();
    }

    @Test
    void shouldNotEvictCachesWhenJdbcExecuteFails() {
        doThrow(new RuntimeException("DB error")).when(jdbc).execute(anyString());

        materializedViewService.refresh();

        verify(cacheManager, never()).getCache(anyString());
    }

    @Test
    void shouldNotFailWhenCacheDoesNotExist() {
        when(cacheManager.getCache("analytics.summary")).thenReturn(null);
        when(cacheManager.getCache("analytics.predictions")).thenReturn(null);
        when(cacheManager.getCache("analytics.anomalies")).thenReturn(null);

        // Should not throw NPE
        materializedViewService.refresh();
    }

    @Test
    void shouldNotFailWhenSomeCachesExistAndSomeDont() {
        when(cacheManager.getCache("analytics.summary")).thenReturn(summaryCache);
        when(cacheManager.getCache("analytics.predictions")).thenReturn(null);
        when(cacheManager.getCache("analytics.anomalies")).thenReturn(anomaliesCache);

        materializedViewService.refresh();

        verify(summaryCache).clear();
        verify(anomaliesCache).clear();
        // predictions cache is null, clear should not be called on it
    }

    @Test
    void shouldEvictAllThreeCachesByCorrectNames() {
        when(cacheManager.getCache("analytics.summary")).thenReturn(summaryCache);
        when(cacheManager.getCache("analytics.predictions")).thenReturn(predictionsCache);
        when(cacheManager.getCache("analytics.anomalies")).thenReturn(anomaliesCache);

        materializedViewService.refresh();

        verify(cacheManager).getCache("analytics.summary");
        verify(cacheManager).getCache("analytics.predictions");
        verify(cacheManager).getCache("analytics.anomalies");
    }

    @Test
    void shouldCallClearOnEachNonNullCache() {
        when(cacheManager.getCache("analytics.summary")).thenReturn(summaryCache);
        when(cacheManager.getCache("analytics.predictions")).thenReturn(predictionsCache);
        when(cacheManager.getCache("analytics.anomalies")).thenReturn(anomaliesCache);

        materializedViewService.refresh();

        verify(summaryCache).clear();
        verify(predictionsCache).clear();
        verify(anomaliesCache).clear();
    }

    @Test
    void refreshSqlShouldContainCorrectTableName() {
        // Verify the SQL constant is well-formed
        String expectedPrefix = "REFRESH MATERIALIZED VIEW CONCURRENTLY";
        // Reflection or simply verify the execute call
        when(cacheManager.getCache(anyString())).thenReturn(null);

        materializedViewService.refresh();

        verify(jdbc).execute("REFRESH MATERIALIZED VIEW CONCURRENTLY mv_job_execution_daily");
    }
}
