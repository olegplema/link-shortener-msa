package com.plema.domain.vo;

import com.plema.domain.exception.InvalidShortUrlIdException;

import java.util.regex.Pattern;

public record ShortUrlId(String value) {
    private static final Pattern PATTERN = Pattern.compile("^[A-Za-z0-9_-]{5,32}$");

    public ShortUrlId {
        if (value == null || !PATTERN.matcher(value).matches()) {
            throw new InvalidShortUrlIdException("Invalid ShortUrlId format.");
        }
    }
}