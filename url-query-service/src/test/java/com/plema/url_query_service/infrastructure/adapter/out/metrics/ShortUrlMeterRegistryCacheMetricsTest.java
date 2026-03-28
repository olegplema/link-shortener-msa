package com.plema.url_query_service.infrastructure.adapter.out.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShortUrlMeterRegistryCacheMetricsTest {

    private SimpleMeterRegistry meterRegistry;
    private ShortUrlMeterRegistryCacheMetrics cacheMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        cacheMetrics = new ShortUrlMeterRegistryCacheMetrics(meterRegistry);
    }

    @Test
    void should_increment_cache_get_unavailable_counter() {
        cacheMetrics.incrementCacheGetUnavailable();

        assertThat(meterRegistry.get("cache_unavailable")
                .tag("operation", "get")
                .counter()
                .count()).isEqualTo(1.0);
    }

    @Test
    void should_increment_cache_put_unavailable_counter() {
        cacheMetrics.incrementCachePutUnavailable();

        assertThat(meterRegistry.get("cache_unavailable")
                .tag("operation", "put")
                .counter()
                .count()).isEqualTo(1.0);
    }
}
