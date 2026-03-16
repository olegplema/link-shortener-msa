package com.plema.url_command_service.application.idempotency;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(alphabetic = true)
public record CreateShortUrlFingerprintPayload(String originalUrl) {
}
