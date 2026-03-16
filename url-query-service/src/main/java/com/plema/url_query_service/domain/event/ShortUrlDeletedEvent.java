package com.plema.url_query_service.domain.event;

import java.time.OffsetDateTime;

public record ShortUrlDeletedEvent(String id, long aggregateVersion, OffsetDateTime createdAt) {
}
