package com.plema.url_command_service.domain.vo;

import com.plema.url_command_service.domain.exception.InvalidUrlException;

public record OriginalUrl(String value) {
    public OriginalUrl {
        if (value == null || !value.startsWith("http")) {
            throw new InvalidUrlException("URL must start with 'http'.");
        }
    }
}