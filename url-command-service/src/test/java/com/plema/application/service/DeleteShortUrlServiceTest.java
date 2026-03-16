package com.plema.application.service;

import com.plema.url_command_service.application.port.out.OutboxRepository;
import com.plema.url_command_service.application.service.DeleteShortUrlService;
import com.plema.url_command_service.domain.event.DomainEvent;
import com.plema.url_command_service.domain.event.ShortUrlDeletedEvent;
import com.plema.url_command_service.domain.exception.InvalidShortUrlIdException;
import com.plema.url_command_service.domain.exception.ShortUrlNotFoundException;
import com.plema.url_command_service.domain.repository.ShortUrlRepository;
import com.plema.url_command_service.domain.vo.ShortUrlId;
import com.plema.testsupport.ShortUrlAggregateTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteShortUrlServiceTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private OutboxRepository outboxRepository;

    private DeleteShortUrlService deleteShortUrlService;

    private OffsetDateTime now;

    @Captor
    private ArgumentCaptor<ShortUrlId> idCaptor;

    @BeforeEach
    void setUp() {
        now = OffsetDateTime.now();
        Clock clock = Clock.fixed(now.toInstant(), now.getOffset());
        deleteShortUrlService = new DeleteShortUrlService(shortUrlRepository, outboxRepository, clock);
    }

    @Test
    void should_delete_short_url_and_publish_event_when_short_url_exists() {
        var id = "abcde1";
        var aggregate = ShortUrlAggregateTestBuilder.aShortUrl()
                .withId(id)
                .buildReconstituted();
        var savedEvents = new ArrayList<DomainEvent>();

        when(shortUrlRepository.findById(any(ShortUrlId.class))).thenReturn(Optional.of(aggregate));
        doAnswer(invocation -> {
            List<DomainEvent> events = invocation.getArgument(0);
            savedEvents.addAll(events);
            return null;
        }).when(outboxRepository).saveEvents(anyList());

        deleteShortUrlService.deleteShortUrl(id);

        verify(shortUrlRepository).findById(idCaptor.capture());
        verify(shortUrlRepository).delete(aggregate);
        verify(outboxRepository).saveEvents(anyList());
        verifyNoMoreInteractions(shortUrlRepository, outboxRepository);

        assertThat(idCaptor.getValue().value()).isEqualTo(id);
        assertThat(savedEvents).hasSize(1);
        assertThat(savedEvents.getFirst()).isInstanceOf(ShortUrlDeletedEvent.class);
        var event = (ShortUrlDeletedEvent) savedEvents.getFirst();
        assertThat(event.id()).isEqualTo(id);
        assertThat(event.aggregateVersion()).isEqualTo(2L);
        assertThat(event.createdAt()).isEqualTo(now);

        assertThat(aggregate.getDomainEvents()).isEmpty();
    }

    @Test
    void should_throw_when_short_url_not_found() {
        var id = "abcde1";

        when(shortUrlRepository.findById(any(ShortUrlId.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteShortUrlService.deleteShortUrl(id))
                .isInstanceOf(ShortUrlNotFoundException.class);

        verify(shortUrlRepository).findById(any(ShortUrlId.class));
        verifyNoInteractions(outboxRepository);
        verifyNoMoreInteractions(shortUrlRepository);
    }

    @Test
    void should_throw_when_short_url_id_invalid() {
        var invalidId = "bad";

        assertThatThrownBy(() -> deleteShortUrlService.deleteShortUrl(invalidId))
                .isInstanceOf(InvalidShortUrlIdException.class);

        verifyNoInteractions(shortUrlRepository, outboxRepository);
    }
}
