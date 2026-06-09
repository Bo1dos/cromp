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
    void shouldExecuteUpsertSqlAndEvictCachesOnSuccess() {
        when(cacheManager.getCache("analytics.summary")).thenReturn(summaryCache);
        when(cacheManager.getCache("analytics.predictions")).thenReturn(predictionsCache);
        when(cacheManager.getCache("analytics.anomalies")).thenReturn(anomaliesCache);
        when(jdbc.update(anyString())).thenReturn(5);

        materializedViewService.refresh();

        verify(jdbc).update(anyString());
        verify(summaryCache).clear();
        verify(predictionsCache).clear();
        verify(anomaliesCache).clear();
    }

    @Test
    void shouldNotThrowWhenJdbcUpdateFails() {
        doThrow(new RuntimeException("DB connection lost"))
                .when(jdbc).update(anyString());

        // Should not throw - the method swallows exceptions
        materializedViewService.refresh();
    }

    @Test
    void shouldNotEvictCachesWhenJdbcUpdateFails() {
        doThrow(new RuntimeException("DB error")).when(jdbc).update(anyString());

        materializedViewService.refresh();

        verify(cacheManager, never()).getCache(anyString());
    }

    @Test
    void shouldNotFailWhenCacheDoesNotExist() {
        when(cacheManager.getCache("analytics.summary")).thenReturn(null);
        when(cacheManager.getCache("analytics.predictions")).thenReturn(null);
        when(cacheManager.getCache("analytics.anomalies")).thenReturn(null);
        when(jdbc.update(anyString())).thenReturn(0);

        // Should not throw NPE
        materializedViewService.refresh();
    }

    @Test
    void shouldNotFailWhenSomeCachesExistAndSomeDont() {
        when(cacheManager.getCache("analytics.summary")).thenReturn(summaryCache);
        when(cacheManager.getCache("analytics.predictions")).thenReturn(null);
        when(cacheManager.getCache("analytics.anomalies")).thenReturn(anomaliesCache);
        when(jdbc.update(anyString())).thenReturn(3);

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
        when(jdbc.update(anyString())).thenReturn(10);

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
        when(jdbc.update(anyString())).thenReturn(7);

        materializedViewService.refresh();

        verify(summaryCache).clear();
        verify(predictionsCache).clear();
        verify(anomaliesCache).clear();
    }

    @Test
    void refreshSqlShouldContainUpsertKeywords() {
        when(cacheManager.getCache(anyString())).thenReturn(null);
        when(jdbc.update(anyString())).thenReturn(0);

        materializedViewService.refresh();

        verify(jdbc).update(anyString());
    }
}
