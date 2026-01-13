package com.plema.url_query_service.domain.event;

import java.time.OffsetDateTime;

public record ShortUrlCreatedEvent(String id, String originalUrl, OffsetDateTime expiration, OffsetDateTime createdAt) {
}