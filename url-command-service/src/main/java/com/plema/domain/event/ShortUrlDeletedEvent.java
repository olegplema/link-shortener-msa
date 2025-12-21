package com.plema.domain.event;

public record ShortUrlDeletedEvent(String id) implements DomainEvent{
}
