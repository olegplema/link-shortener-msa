package com.plema.url_query_service.application.service;

import com.plema.url_query_service.application.port.out.ShortUrlCache;
import com.plema.url_query_service.domain.model.ShortUrlReadModel;
import com.plema.url_query_service.domain.repository.ShortUrlRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetShortUrlServiceTest {

    @Mock
    private ShortUrlCache cache;

    @Mock
    private ShortUrlRepository repository;

    private SimpleMeterRegistry meterRegistry;
    private GetShortUrlService getShortUrlService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        getShortUrlService = new GetShortUrlService(cache, repository, meterRegistry);
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
        assertThat(meterRegistry.get("cache_unavailable")
                .tag("operation", "get")
                .counter()
                .count()).isEqualTo(1.0);
        verify(repository).findById(id);
        verify(cache).put(eq(id), eq(model), any());
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
        assertThat(meterRegistry.get("cache_unavailable")
                .tag("operation", "put")
                .counter()
                .count()).isEqualTo(1.0);
        verify(repository).findById(id);
        verify(cache).put(eq(id), eq(model), any());
    }
}
