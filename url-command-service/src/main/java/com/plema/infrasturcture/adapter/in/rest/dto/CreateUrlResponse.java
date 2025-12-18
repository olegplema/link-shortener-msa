package com.plema.infrasturcture.adapter.in.rest.dto;

import java.time.OffsetDateTime;

public record CreateUrlResponse(String id, String originalUrl, OffsetDateTime expiration) {
}
