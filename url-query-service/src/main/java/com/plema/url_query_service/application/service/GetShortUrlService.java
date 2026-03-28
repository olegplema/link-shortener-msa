package com.plema.url_query_service.application.service;

import com.plema.url_query_service.application.port.out.ShortUrlCacheMetrics;
import com.plema.url_query_service.application.port.out.ShortUrlCache;
import com.plema.url_query_service.domain.model.ShortUrlReadModel;
import com.plema.url_query_service.domain.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetShortUrlService {
    private final ShortUrlCache cache;
    private final ShortUrlRepository repository;
    private final ShortUrlCacheMetrics cacheMetrics;

    public Optional<ShortUrlReadModel> findById(String id) {
        var cachedModel = getFromCache(id);
        if (cachedModel.isPresent()) {
            return cachedModel;
        }

        return repository.findById(id)
                .map(model -> {
                    putInCache(id, model);
                    return model;
                });
    }

    private Duration calculateTtl(OffsetDateTime expiration) {
        var now = OffsetDateTime.now();
        var duration = Duration.between(now, expiration);

        if (duration.isNegative() || duration.isZero()) {
            return Duration.ofSeconds(1);
        }

        return duration;
    }

    private Optional<ShortUrlReadModel> getFromCache(String id) {
        try {
            return cache.get(id);
        } catch (RuntimeException ex) {
            cacheMetrics.incrementCacheGetUnavailable();
            return Optional.empty();
        }
    }

    private void putInCache(String id, ShortUrlReadModel model) {
        try {
            cache.put(id, model, calculateTtl(model.expiration()));
        } catch (RuntimeException ex) {
            cacheMetrics.incrementCachePutUnavailable();
        }
    }

}
