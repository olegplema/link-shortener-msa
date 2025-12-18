package com.plema.domain.event;

import com.plema.domain.vo.Expiration;
import com.plema.domain.vo.OriginalUrl;
import com.plema.domain.vo.ShortUrlId;

import java.time.OffsetDateTime;

public record ShortUrlCreatedEvent(String id, String originalUrl, OffsetDateTime expiration) implements DomainEvent {
}