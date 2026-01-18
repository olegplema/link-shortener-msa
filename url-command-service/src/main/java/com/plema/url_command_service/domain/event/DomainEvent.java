package com.plema.url_command_service.domain.event;

public sealed interface DomainEvent permits ShortUrlCreatedEvent, ShortUrlDeletedEvent {
}