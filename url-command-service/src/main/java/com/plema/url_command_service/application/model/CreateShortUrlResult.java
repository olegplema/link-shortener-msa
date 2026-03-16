package com.plema.url_command_service.application.model;

import java.time.OffsetDateTime;

public record CreateShortUrlResult(String id, String originalUrl, OffsetDateTime expiration) {
}
