package com.plema.url_query_service.infrastructure.adapter.out.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import com.plema.url_query_service.application.port.out.ShortUrlCacheMetrics;

@Component
@AllArgsConstructor
public class ShortUrlMeterRegistryCacheMetrics implements ShortUrlCacheMetrics {
    private static final String CACHE_HIT_METRIC = "cache_hit_total";
    private static final String CACHE_MISS_METRIC = "cache_miss_total";
    private static final String CACHE_UNAVAILABLE_METRIC = "cache_unavailable";
    private static final String OPERATION_TAG = "operation";
    private static final String GET_OPERATION = "get";
    private static final String PUT_OPERATION = "put";

    private final MeterRegistry meterRegistry;

    @Override
    public void incrementCacheHit() {
        meterRegistry.counter(CACHE_HIT_METRIC).increment();
    }

    @Override
    public void incrementCacheMiss() {
        meterRegistry.counter(CACHE_MISS_METRIC).increment();
    }

    @Override
    public void incrementCacheGetUnavailable() {
        meterRegistry.counter(CACHE_UNAVAILABLE_METRIC, OPERATION_TAG, GET_OPERATION).increment();
    }

    @Override
    public void incrementCachePutUnavailable() {
        meterRegistry.counter(CACHE_UNAVAILABLE_METRIC, OPERATION_TAG, PUT_OPERATION).increment();
    }
}
