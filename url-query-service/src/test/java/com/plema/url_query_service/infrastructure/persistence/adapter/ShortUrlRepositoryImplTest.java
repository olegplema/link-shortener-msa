package com.plema.url_query_service.infrastructure.persistence.adapter;

import com.plema.url_query_service.domain.model.ShortUrlReadModel;
import com.plema.url_query_service.infrastructure.persistence.entity.ShortUrlEntity;
import com.plema.url_query_service.infrastructure.persistence.jpa.SpringDataShortUrlQueryRepository;
import com.plema.url_query_service.infrastructure.persistence.mapper.ShortUrlMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShortUrlRepositoryImplTest {

    @Mock
    private SpringDataShortUrlQueryRepository shortUrlQueryRepository;

    private ShortUrlRepositoryImpl shortUrlRepository;

    @BeforeEach
    void setUp() {
        ShortUrlMapper shortUrlMapper = Mappers.getMapper(ShortUrlMapper.class);
        shortUrlRepository = new ShortUrlRepositoryImpl(shortUrlQueryRepository, shortUrlMapper);
    }

    @Test
    void should_save_created_event_when_record_missing() {
        var createdAt = OffsetDateTime.now();
        var readModel = new ShortUrlReadModel(
                "abc123",
                "https://example.com",
                createdAt.plusDays(7),
                0,
                createdAt,
                1L,
                false
        );

        when(shortUrlQueryRepository.findById(readModel.id())).thenReturn(Optional.empty());

        shortUrlRepository.applyCreated(readModel);

        var captor = ArgumentCaptor.forClass(ShortUrlEntity.class);
        verify(shortUrlQueryRepository).save(captor.capture());
        assertThat(captor.getValue().getAggregateVersion()).isEqualTo(1L);
        assertThat(captor.getValue().isDeleted()).isFalse();
    }

    @Test
    void should_ignore_created_event_when_existing_version_is_newer() {
        var createdAt = OffsetDateTime.now();
        var readModel = new ShortUrlReadModel(
                "abc123",
                "https://example.com",
                createdAt.plusDays(7),
                0,
                createdAt,
                1L,
                false
        );
        var existing = new ShortUrlEntity();
        existing.setId(readModel.id());
        existing.setAggregateVersion(2L);
        existing.setDeleted(true);

        when(shortUrlQueryRepository.findById(readModel.id())).thenReturn(Optional.of(existing));

        shortUrlRepository.applyCreated(readModel);

        verify(shortUrlQueryRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void should_create_tombstone_when_delete_arrives_before_create() {
        var deletedAt = OffsetDateTime.now();

        when(shortUrlQueryRepository.findById("abc123")).thenReturn(Optional.empty());

        shortUrlRepository.applyDeleted("abc123", 2L, deletedAt);

        var captor = ArgumentCaptor.forClass(ShortUrlEntity.class);
        verify(shortUrlQueryRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo("abc123");
        assertThat(captor.getValue().getAggregateVersion()).isEqualTo(2L);
        assertThat(captor.getValue().isDeleted()).isTrue();
        assertThat(captor.getValue().getClickCount()).isEqualTo(0);
    }

    @Test
    void should_ignore_stale_delete_event() {
        var existing = new ShortUrlEntity();
        existing.setId("abc123");
        existing.setAggregateVersion(2L);
        existing.setDeleted(true);
        existing.setClickCount(0);

        when(shortUrlQueryRepository.findById("abc123")).thenReturn(Optional.of(existing));

        shortUrlRepository.applyDeleted("abc123", 1L, OffsetDateTime.now());

        verify(shortUrlQueryRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
