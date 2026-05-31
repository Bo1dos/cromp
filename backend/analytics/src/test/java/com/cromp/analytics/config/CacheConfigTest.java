package com.cromp.analytics.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    @Test
    void shouldCreateCacheManagerWithExpectedCaches() {
        CacheConfig config = new CacheConfig();

        // Inject values via reflection or test defaults
        setField(config, "ttlMinutes", 60L);
        setField(config, "maxSize", 500L);

        CacheManager cacheManager = config.cacheManager();

        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getCacheNames())
                .containsExactlyInAnyOrder(
                        "analytics.summary",
                        "analytics.predictions",
                        "analytics.anomalies"
                );
    }

    @Test
    void shouldCreateNonNullCaches() {
        CacheConfig config = new CacheConfig();
        setField(config, "ttlMinutes", 60L);
        setField(config, "maxSize", 500L);

        CacheManager cacheManager = config.cacheManager();

        assertThat(cacheManager.getCache("analytics.summary")).isNotNull();
        assertThat(cacheManager.getCache("analytics.predictions")).isNotNull();
        assertThat(cacheManager.getCache("analytics.anomalies")).isNotNull();
    }

    @Test
    void shouldUseDefaultValues() {
        CacheConfig config = new CacheConfig();
        // Default: ttl=60, maxSize=500
        CacheManager cacheManager = config.cacheManager();

        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getCacheNames()).isNotEmpty();
    }

    @Test
    void shouldWorkWithDifferentTtlAndSize() {
        CacheConfig config = new CacheConfig();
        setField(config, "ttlMinutes", 10L);
        setField(config, "maxSize", 100L);

        CacheManager cacheManager = config.cacheManager();

        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getCacheNames()).hasSize(3);
    }

    @Test
    void cacheManagerShouldBeCaffeineType() {
        CacheConfig config = new CacheConfig();
        setField(config, "ttlMinutes", 60L);
        setField(config, "maxSize", 500L);

        CacheManager cacheManager = config.cacheManager();

        assertThat(cacheManager).isInstanceOf(CaffeineCacheManager.class);
    }

    @Test
    void shouldCreateDistinctCacheInstances() {
        CacheConfig config = new CacheConfig();
        setField(config, "ttlMinutes", 60L);
        setField(config, "maxSize", 500L);

        CacheManager cacheManager = config.cacheManager();

        Cache summary = cacheManager.getCache("analytics.summary");
        Cache predictions = cacheManager.getCache("analytics.predictions");
        Cache anomalies = cacheManager.getCache("analytics.anomalies");

        assertThat(summary).isNotSameAs(predictions);
        assertThat(summary).isNotSameAs(anomalies);
        assertThat(predictions).isNotSameAs(anomalies);
    }

    @Test
    void shouldAllowCacheOperations() {
        CacheConfig config = new CacheConfig();
        setField(config, "ttlMinutes", 60L);
        setField(config, "maxSize", 500L);

        CacheManager cacheManager = config.cacheManager();
        Cache cache = cacheManager.getCache("analytics.summary");

        assertThat(cache).isNotNull();
        // Put and get should work
        cache.put("test-key", "test-value");
        assertThat(cache.get("test-key", String.class)).isEqualTo("test-value");
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
