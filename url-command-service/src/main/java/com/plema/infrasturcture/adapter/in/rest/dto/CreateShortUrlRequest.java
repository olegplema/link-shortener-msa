package com.plema.infrasturcture.adapter.in.rest.dto;

import java.time.OffsetDateTime;

public record CreateShortUrlRequest(String originalUrl, OffsetDateTime expiration) {

}
