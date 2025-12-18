package com.plema.domain.vo;

import com.plema.domain.exception.InvalidExpirationException;

import java.time.OffsetDateTime;

public record Expiration(OffsetDateTime value) {
    public Expiration {
        if (value != null && value.isBefore(OffsetDateTime.now())) {
            throw new InvalidExpirationException("Expiration date must be in the future.");
        }
    }
}