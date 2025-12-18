package com.plema.domain.event;

public sealed interface DomainEvent permits ShortUrlCreatedEvent, ShortUrlDeletedEvent {
}