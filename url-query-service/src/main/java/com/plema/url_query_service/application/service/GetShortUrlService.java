package com.plema.url_query_service.application.service;

import io.micrometer.core.instrument.MeterRegistry;
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
    private static final String CACHE_UNAVAILABLE_METRIC = "cache_unavailable";
    private static final String OPERATION_TAG = "operation";
    private static final String GET_OPERATION = "get";
    private static final String PUT_OPERATION = "put";

    private final ShortUrlCache cache;
    private final ShortUrlRepository repository;
    private final MeterRegistry meterRegistry;

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
            incrementCacheUnavailable(GET_OPERATION);
            return Optional.empty();
        }
    }

    private void putInCache(String id, ShortUrlReadModel model) {
        try {
            cache.put(id, model, calculateTtl(model.expiration()));
        } catch (RuntimeException ex) {
            incrementCacheUnavailable(PUT_OPERATION);
        }
    }

    private void incrementCacheUnavailable(String operation) {
        meterRegistry.counter(CACHE_UNAVAILABLE_METRIC, OPERATION_TAG, operation).increment();
    }
}
