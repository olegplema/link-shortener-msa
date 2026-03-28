package com.plema.url_query_service.application.port.out;

public interface ShortUrlCacheMetrics {
    void incrementCacheGetUnavailable();

    void incrementCachePutUnavailable();
}