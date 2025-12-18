package com.plema.domain.exception;

public class InvalidShortUrlIdException extends RuntimeException {
    public InvalidShortUrlIdException(String message) {
        super(message);
    }
}
