package com.plema.url_command_service.infrasturcture.adapter.in.rest.dto;

import java.time.OffsetDateTime;

public record CreateUrlResponse(String id, String originalUrl, OffsetDateTime expiration) {
}
