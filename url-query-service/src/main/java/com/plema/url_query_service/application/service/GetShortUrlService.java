package com.plema.url_query_service.application.service;

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

    public Optional<ShortUrlReadModel> findById(String id) {
        return cache.get(id)
                .or(() -> repository.findById(id)
                        .map(model -> {
                            cache.put(id, model, calculateTtl(model.expiration()));
                            return model;
                        })
                );
    }

    private Duration calculateTtl(OffsetDateTime expiration) {
        var now = OffsetDateTime.now();
        var duration = Duration.between(now, expiration);

        if (duration.isNegative() || duration.isZero()) {
            return Duration.ofSeconds(1);
        }

        return duration;
    }
}
