package com.plema.domain.aggregate;

import com.plema.domain.event.ShortUrlCreatedEvent;
import com.plema.domain.event.ShortUrlDeletedEvent;
import com.plema.testsupport.ShortUrlAggregateTestBuilder;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShortUrlAggregateTest {

    @Test
    void should_register_created_event_when_created() {
        var id = "abcde1";
        var url = "http://example.com";
        var expiration = OffsetDateTime.now().plusDays(1);

        var aggregate = ShortUrlAggregate.create(id, url, expiration);

        assertThat(aggregate.getId().value()).isEqualTo(id);
        assertThat(aggregate.getOriginalUrl().value()).isEqualTo(url);
        assertThat(aggregate.getExpiration().value()).isEqualTo(expiration);
        assertThat(aggregate.getDomainEvents()).hasSize(1);

        var event = aggregate.getDomainEvents().get(0);
        assertThat(event).isInstanceOf(ShortUrlCreatedEvent.class);
        var createdEvent = (ShortUrlCreatedEvent) event;
        assertThat(createdEvent.id()).isEqualTo(id);
        assertThat(createdEvent.originalUrl()).isEqualTo(url);
        assertThat(createdEvent.expiration()).isEqualTo(expiration);
    }

    @Test
    void should_register_deleted_event_when_deleted() {
        var aggregate = ShortUrlAggregateTestBuilder.aShortUrl().buildReconstituted();

        aggregate.delete();

        assertThat(aggregate.getDomainEvents()).hasSize(1);
        var event = aggregate.getDomainEvents().get(0);
        assertThat(event).isInstanceOf(ShortUrlDeletedEvent.class);
        var deletedEvent = (ShortUrlDeletedEvent) event;
        assertThat(deletedEvent.id()).isEqualTo(aggregate.getId().value());
    }

    @Test
    void should_clear_domain_events_when_clear_called() {
        var aggregate = ShortUrlAggregateTestBuilder.aShortUrl().buildCreated();

        aggregate.clearDomainEvents();

        assertThat(aggregate.getDomainEvents()).isEmpty();
    }

    @Test
    void should_return_unmodifiable_domain_events_list_when_accessed() {
        var aggregate = ShortUrlAggregateTestBuilder.aShortUrl().buildCreated();

        var events = aggregate.getDomainEvents();

        assertThatThrownBy(() -> events.add(new ShortUrlDeletedEvent(aggregate.getId().value())))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_not_register_domain_events_when_reconstituted() {
        var aggregate = ShortUrlAggregateTestBuilder.aShortUrl().buildReconstituted();

        assertThat(aggregate.getDomainEvents()).isEmpty();
    }
}
