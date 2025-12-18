package com.plema.domain.vo;

import com.plema.domain.exception.InvalidUrlException;

public record OriginalUrl(String value) {
    public OriginalUrl {
        if (value == null || !value.startsWith("http")) {
            throw new InvalidUrlException("URL must start with 'http'.");
        }
    }
}