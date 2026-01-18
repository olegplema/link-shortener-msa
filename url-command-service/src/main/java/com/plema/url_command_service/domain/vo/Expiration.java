package com.plema.url_command_service.domain.vo;

import com.plema.url_command_service.domain.exception.InvalidExpirationException;

import java.time.OffsetDateTime;

public record Expiration(OffsetDateTime value) {
    public Expiration {
        if (value != null && value.isBefore(OffsetDateTime.now())) {
            throw new InvalidExpirationException("Expiration date must be in the future.");
        }
    }
}