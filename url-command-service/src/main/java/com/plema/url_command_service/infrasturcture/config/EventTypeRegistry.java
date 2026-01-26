package com.plema.url_command_service.infrasturcture.config;

import com.plema.url_command_service.domain.event.DomainEvent;
import com.plema.url_command_service.domain.event.ShortUrlCreatedEvent;
import com.plema.url_command_service.domain.event.ShortUrlDeletedEvent;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EventTypeRegistry {

    public static String resolveAggregateType(DomainEvent event) {
        return switch (event) {
            case ShortUrlCreatedEvent _, ShortUrlDeletedEvent _ -> "shorturl";
        };
    }

    public String resolveEventType(DomainEvent event) {
        return switch (event) {
            case ShortUrlCreatedEvent _ -> "created.v1";
            case ShortUrlDeletedEvent _ -> "deleted.v1";
        };
    }
}