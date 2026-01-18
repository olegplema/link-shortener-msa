package com.plema.url_command_service.domain.exception;

public class InvalidShortUrlIdException extends RuntimeException {
    public InvalidShortUrlIdException(String message) {
        super(message);
    }
}
