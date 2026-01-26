package com.plema.url_command_service.domain.event;

import java.time.OffsetDateTime;

public record ShortUrlDeletedEvent(String id, OffsetDateTime createdAt) implements DomainEvent{
}
