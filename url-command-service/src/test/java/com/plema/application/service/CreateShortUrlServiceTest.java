package com.plema.application.service;

import com.plema.url_command_service.application.port.out.OutboxRepository;
import com.plema.url_command_service.application.port.out.ShortUrlIdGenerator;
import com.plema.url_command_service.application.service.CreateShortUrlService;
import com.plema.url_command_service.domain.aggregate.ShortUrlAggregate;
import com.plema.url_command_service.domain.event.DomainEvent;
import com.plema.url_command_service.domain.event.ShortUrlCreatedEvent;
import com.plema.url_command_service.domain.exception.InvalidUrlException;
import com.plema.url_command_service.domain.exception.UrlIdExistsException;
import com.plema.url_command_service.domain.repository.ShortUrlRepository;
import com.plema.url_command_service.domain.service.UrlUniquenessChecker;
import com.plema.url_command_service.domain.vo.ShortUrlId;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CreateShortUrlServiceTest {
    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private UrlUniquenessChecker urlUniquenessChecker;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private ShortUrlIdGenerator shortUrlIdGenerator;

    private CreateShortUrlService createShortUrlService;

    private OffsetDateTime now;

    @Captor
    private ArgumentCaptor<ShortUrlAggregate> aggregateCaptor;

    @Captor
    private ArgumentCaptor<ShortUrlId> idCaptor;

    @BeforeEach
    void setUp() {
        now = OffsetDateTime.now();
        Clock clock = Clock.fixed(now.toInstant(), now.getOffset());
        createShortUrlService = new CreateShortUrlService(
                shortUrlRepository,
                urlUniquenessChecker,
                outboxRepository,
                shortUrlIdGenerator,
                clock
        );
    }

    @Test
    void should_create_short_url_and_publish_event_when_request_valid() {
        var url = "http://example.com";
        var expectedId = "abc123";
        var expectedExpiration = now.plusDays(7);
        var savedEvents = new ArrayList<DomainEvent>();

        org.mockito.Mockito.when(shortUrlIdGenerator.nextId()).thenReturn(expectedId);

        doAnswer(invocation -> {
            List<DomainEvent> events = invocation.getArgument(0);
            savedEvents.addAll(events);
            return null;
        }).when(outboxRepository).saveEvents(anyList());

        var result = createShortUrlService.createShortUrl(url);

        verify(shortUrlRepository).save(aggregateCaptor.capture());
        verify(urlUniquenessChecker).ensureUnique(idCaptor.capture());
        verify(outboxRepository).saveEvents(anyList());

        var savedAggregate = aggregateCaptor.getValue();
        assertThat(savedAggregate.getOriginalUrl().value()).isEqualTo(url);
        assertThat(savedAggregate.getId().value()).isEqualTo(expectedId);
        assertThat(savedAggregate.getExpiration().value()).isEqualTo(expectedExpiration);
        assertThat(savedAggregate.getCreatedAt().value()).isEqualTo(now);
        assertThat(idCaptor.getValue().value()).isEqualTo(savedAggregate.getId().value());

        assertThat(savedEvents).hasSize(1);
        assertThat(savedEvents.getFirst()).isInstanceOf(ShortUrlCreatedEvent.class);
        var event = (ShortUrlCreatedEvent) savedEvents.getFirst();
        assertThat(event.id()).isEqualTo(savedAggregate.getId().value());
        assertThat(event.originalUrl()).isEqualTo(url);
        assertThat(event.expiration()).isEqualTo(expectedExpiration);
        assertThat(event.aggregateVersion()).isEqualTo(1L);
        assertThat(event.createdAt()).isEqualTo(now);

        assertThat(result.getDomainEvents()).isEmpty();
    }

    @Test
    void should_throw_when_url_invalid() {
        var invalidUrl = "ftp://example.com";

        org.mockito.Mockito.when(shortUrlIdGenerator.nextId()).thenReturn("abc123");

        assertThatThrownBy(() -> createShortUrlService.createShortUrl(invalidUrl))
                .isInstanceOf(InvalidUrlException.class);

        verifyNoInteractions(urlUniquenessChecker, shortUrlRepository, outboxRepository);
    }

    @Test
    void should_throw_when_short_url_id_already_exists() {
        var url = "http://example.com";

        org.mockito.Mockito.when(shortUrlIdGenerator.nextId()).thenReturn("abc123");

        doThrow(new UrlIdExistsException("Short URL already exists."))
                .when(urlUniquenessChecker)
                .ensureUnique(any(ShortUrlId.class));

        assertThatThrownBy(() -> createShortUrlService.createShortUrl(url))
                .isInstanceOf(UrlIdExistsException.class);

        verify(urlUniquenessChecker).ensureUnique(any(ShortUrlId.class));
        verifyNoInteractions(shortUrlRepository, outboxRepository);
    }
}
