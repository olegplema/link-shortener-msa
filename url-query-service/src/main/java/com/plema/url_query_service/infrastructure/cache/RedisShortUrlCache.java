package com.plema.url_query_service.infrastructure.cache;

import com.plema.url_query_service.application.port.out.ShortUrlCache;
import com.plema.url_query_service.domain.model.ShortUrlReadModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisShortUrlCache implements ShortUrlCache {

    private static final String KEY_PREFIX = "short-url:";

    private final RedisTemplate<String, ShortUrlReadModel> redisTemplate;

    @Override
    public Optional<ShortUrlReadModel> get(String id) {
        var key = KEY_PREFIX + id;
        var value = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(value);
    }

    @Override
    public void put(String id, ShortUrlReadModel model, Duration ttl) {
        var key = KEY_PREFIX + id;
        redisTemplate.opsForValue().set(key, model, ttl);
    }

    @Override
    public void evict(String id) {
        var key = KEY_PREFIX + id;
        redisTemplate.delete(key);
    }

    @Override
    public void clear() {
        var keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}