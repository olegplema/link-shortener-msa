package com.plema.application.service;

import com.plema.application.port.out.OutboxRepository;
import com.plema.domain.aggregate.ShortUrlAggregate;
import com.plema.domain.event.DomainEvent;
import com.plema.domain.event.ShortUrlCreatedEvent;
import com.plema.domain.exception.InvalidExpirationException;
import com.plema.domain.exception.InvalidUrlException;
import com.plema.domain.exception.UrlIdExistsException;
import com.plema.domain.repository.ShortUrlRepository;
import com.plema.domain.service.UrlUniquenessChecker;
import com.plema.domain.vo.ShortUrlId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private CreateShortUrlService createShortUrlService;

    @Captor
    private ArgumentCaptor<ShortUrlAggregate> aggregateCaptor;

    @Captor
    private ArgumentCaptor<ShortUrlId> idCaptor;

    @Test
    void should_create_short_url_and_publish_event_when_request_valid() {
        var url = "http://example.com";
        var expiration = OffsetDateTime.now().plusDays(1);
        var savedEvents = new ArrayList<DomainEvent>();

        doAnswer(invocation -> {
            List<DomainEvent> events = invocation.getArgument(0);
            savedEvents.addAll(events);
            return null;
        }).when(outboxRepository).saveEvents(anyList());

        var result = createShortUrlService.createShortUrl(url, expiration);

        verify(shortUrlRepository).save(aggregateCaptor.capture());
        verify(urlUniquenessChecker).ensureUnique(idCaptor.capture());
        verify(outboxRepository).saveEvents(anyList());

        var savedAggregate = aggregateCaptor.getValue();
        assertThat(savedAggregate.getOriginalUrl().value()).isEqualTo(url);
        assertThat(savedAggregate.getExpiration().value()).isEqualTo(expiration);
        assertThat(idCaptor.getValue().value()).isEqualTo(savedAggregate.getId().value());

        assertThat(savedEvents).hasSize(1);
        assertThat(savedEvents.getFirst()).isInstanceOf(ShortUrlCreatedEvent.class);
        var event = (ShortUrlCreatedEvent) savedEvents.getFirst();
        assertThat(event.id()).isEqualTo(savedAggregate.getId().value());
        assertThat(event.originalUrl()).isEqualTo(url);
        assertThat(event.expiration()).isEqualTo(expiration);

        assertThat(result.getDomainEvents()).isEmpty();
    }

    @Test
    void should_throw_when_url_invalid() {
        var invalidUrl = "ftp://example.com";

        assertThatThrownBy(() -> createShortUrlService.createShortUrl(invalidUrl, OffsetDateTime.now().plusDays(1)))
                .isInstanceOf(InvalidUrlException.class);

        verifyNoInteractions(urlUniquenessChecker, shortUrlRepository, outboxRepository);
    }

    @Test
    void should_throw_when_expiration_in_past() {
        var url = "http://example.com";
        var pastExpiration = OffsetDateTime.now().minusDays(1);

        assertThatThrownBy(() -> createShortUrlService.createShortUrl(url, pastExpiration))
                .isInstanceOf(InvalidExpirationException.class);

        verifyNoInteractions(urlUniquenessChecker, shortUrlRepository, outboxRepository);
    }

    @Test
    void should_throw_when_short_url_id_already_exists() {
        var url = "http://example.com";
        var expiration = OffsetDateTime.now().plusDays(1);

        doThrow(new UrlIdExistsException("Short URL already exists."))
                .when(urlUniquenessChecker)
                .ensureUnique(any(ShortUrlId.class));

        assertThatThrownBy(() -> createShortUrlService.createShortUrl(url, expiration))
                .isInstanceOf(UrlIdExistsException.class);

        verify(urlUniquenessChecker).ensureUnique(any(ShortUrlId.class));
        verifyNoInteractions(shortUrlRepository, outboxRepository);
    }
}
