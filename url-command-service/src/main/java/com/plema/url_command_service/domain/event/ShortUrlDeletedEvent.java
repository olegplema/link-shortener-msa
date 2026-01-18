package com.plema.url_command_service.domain.event;

public record ShortUrlDeletedEvent(String id) implements DomainEvent{
}
