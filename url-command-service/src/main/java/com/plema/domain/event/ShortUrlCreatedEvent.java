package com.plema.domain.event;

import java.time.OffsetDateTime;

public record ShortUrlCreatedEvent(String id, String originalUrl, OffsetDateTime expiration, OffsetDateTime createdAt) implements DomainEvent {
}