package com.plema.url_query_service.application.service;

import com.plema.url_query_service.application.port.out.ShortUrlCache;
import com.plema.url_query_service.application.port.out.ShortUrlCacheMetrics;
import com.plema.url_query_service.domain.model.ShortUrlReadModel;
import com.plema.url_query_service.domain.repository.ShortUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetShortUrlServiceTest {

    @Mock
    private ShortUrlCache cache;

    @Mock
    private ShortUrlRepository repository;

    @Mock
    private ShortUrlCacheMetrics cacheMetrics;

    private GetShortUrlService getShortUrlService;

    @BeforeEach
    void setUp() {
        getShortUrlService = new GetShortUrlService(cache, repository, cacheMetrics);
    }

    @Test
    void shouldFallbackToRepositoryWhenCacheGetFails() {
        var id = "abc123";
        var model = new ShortUrlReadModel(
                id,
                "https://example.com",
                OffsetDateTime.now().plusDays(7),
                0,
                OffsetDateTime.now(),
                1L,
                false
        );

        when(cache.get(id)).thenThrow(new RuntimeException("redis unavailable"));
        when(repository.findById(id)).thenReturn(Optional.of(model));

        var result = getShortUrlService.findById(id);

        assertThat(result).contains(model);
        verify(cacheMetrics).incrementCacheGetUnavailable();
        verify(cacheMetrics, never()).incrementCacheMiss();
        verify(repository).findById(id);
        verify(cache).put(eq(id), eq(model), any());
    }

    @Test
    void should_record_cache_hit_when_value_is_found_in_cache() {
        var id = "hit123";
        var model = new ShortUrlReadModel(
                id,
                "https://example.com",
                OffsetDateTime.now().plusDays(7),
                0,
                OffsetDateTime.now(),
                1L,
                false
        );

        when(cache.get(id)).thenReturn(Optional.of(model));

        var result = getShortUrlService.findById(id);

        assertThat(result).contains(model);
        verify(cacheMetrics).incrementCacheHit();
        verify(repository, never()).findById(any());
    }

    @Test
    void should_record_cache_miss_when_cache_is_empty() {
        var id = "miss123";

        when(cache.get(id)).thenReturn(Optional.empty());
        when(repository.findById(id)).thenReturn(Optional.empty());

        var result = getShortUrlService.findById(id);

        assertThat(result).isEmpty();
        verify(cacheMetrics).incrementCacheMiss();
    }

    @Test
    void shouldReturnResultWhenCachePutFails() {
        var id = "xyz789";
        var model = new ShortUrlReadModel(
                id,
                "https://example.org",
                OffsetDateTime.now().plusDays(7),
                0,
                OffsetDateTime.now(),
                1L,
                false
        );

        when(cache.get(id)).thenReturn(Optional.empty());
        when(repository.findById(id)).thenReturn(Optional.of(model));
        doThrow(new RuntimeException("redis unavailable")).when(cache).put(eq(id), eq(model), any());

        var result = getShortUrlService.findById(id);

        assertThat(result).contains(model);
        verify(cacheMetrics).incrementCachePutUnavailable();
        verify(repository).findById(id);
        verify(cache).put(eq(id), eq(model), any());
    }
}
