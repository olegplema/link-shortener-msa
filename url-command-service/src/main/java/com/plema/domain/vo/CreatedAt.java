package com.plema.domain.vo;

import java.time.OffsetDateTime;

public record CreatedAt(OffsetDateTime value) {
    public CreatedAt {
        if (value == null) {
            throw new IllegalArgumentException("CreatedAt cannot be null");
        }
    }
}