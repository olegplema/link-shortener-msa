package com.plema.url_command_service.domain.event;

import java.time.OffsetDateTime;

public record ShortUrlCreatedEvent(
        String id,
        String originalUrl,
        OffsetDateTime expiration,
        long aggregateVersion,
        OffsetDateTime createdAt
) implements DomainEvent {
}
