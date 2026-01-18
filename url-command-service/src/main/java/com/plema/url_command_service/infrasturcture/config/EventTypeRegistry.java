package com.plema.url_command_service.infrasturcture.config;

import com.plema.url_command_service.domain.event.DomainEvent;
import com.plema.url_command_service.domain.event.ShortUrlCreatedEvent;
import com.plema.url_command_service.domain.event.ShortUrlDeletedEvent;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EventTypeRegistry {

    public String resolveType(DomainEvent event) {
        return switch (event) {
            case ShortUrlCreatedEvent _ -> "plema.url.created.v1";
            case ShortUrlDeletedEvent _ -> "plema.url.deleted.v1";
        };
    }
}