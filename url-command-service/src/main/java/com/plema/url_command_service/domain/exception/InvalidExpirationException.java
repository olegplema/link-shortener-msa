package com.plema.url_command_service.domain.exception;

public class InvalidExpirationException extends RuntimeException {
    public InvalidExpirationException(String message) {
        super(message);
    }
}
