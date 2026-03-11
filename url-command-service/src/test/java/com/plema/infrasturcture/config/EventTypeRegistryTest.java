package com.plema.infrasturcture.config;

import com.plema.url_command_service.domain.event.ShortUrlCreatedEvent;
import com.plema.url_command_service.domain.event.ShortUrlDeletedEvent;
import com.plema.url_command_service.infrasturcture.config.EventTypeRegistry;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventTypeRegistryTest {

    @Test
    void should_resolve_type_for_short_url_created_event() {
        var createdAt = OffsetDateTime.now();
        var expiration = createdAt.plusDays(1);
        var event = new ShortUrlCreatedEvent("abcde1", "http://example.com", expiration, createdAt);

        var type = EventTypeRegistry.resolveEventType(event);

        assertThat(type).isEqualTo("created.v1");
    }

    @Test
    void should_resolve_type_for_short_url_deleted_event() {
        var event = new ShortUrlDeletedEvent("abcde1", OffsetDateTime.now());

        var type = EventTypeRegistry.resolveEventType(event);

        assertThat(type).isEqualTo("deleted.v1");
    }
}
