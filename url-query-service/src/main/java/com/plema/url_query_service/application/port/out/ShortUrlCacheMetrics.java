package com.plema.url_query_service.application.port.out;

public interface ShortUrlCacheMetrics {
    void incrementCacheHit();

    void incrementCacheMiss();

    void incrementCacheGetUnavailable();

    void incrementCachePutUnavailable();
}
