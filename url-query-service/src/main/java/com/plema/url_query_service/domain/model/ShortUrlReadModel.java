package com.plema.url_query_service.domain.model;

import java.time.OffsetDateTime;

public record ShortUrlReadModel(String id,
                                String originalUrl,
                                OffsetDateTime expiration,
                                Integer clickCount,
                                OffsetDateTime createdAt
) {
}

