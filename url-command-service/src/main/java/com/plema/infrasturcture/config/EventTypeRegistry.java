package com.plema.infrasturcture.config;

import com.plema.domain.event.DomainEvent;
import com.plema.domain.event.ShortUrlCreatedEvent;
import com.plema.domain.event.ShortUrlDeletedEvent;
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