package com.plema.url_query_service.application.port.out;

import com.plema.url_query_service.domain.model.ShortUrlReadModel;

import java.time.Duration;
import java.util.Optional;

public interface ShortUrlCache {
    Optional<ShortUrlReadModel> get(String id);
    void put(String id, ShortUrlReadModel model, Duration ttl);
    void evict(String id);
}
