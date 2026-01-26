package com.plema.url_command_service.domain.event;

import java.time.OffsetDateTime;

public sealed interface DomainEvent permits ShortUrlCreatedEvent, ShortUrlDeletedEvent {
    String id();
    OffsetDateTime createdAt();
}