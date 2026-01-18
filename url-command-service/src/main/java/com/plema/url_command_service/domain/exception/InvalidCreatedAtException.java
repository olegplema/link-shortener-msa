package com.plema.url_command_service.domain.exception;

public class InvalidCreatedAtException extends RuntimeException {
    public InvalidCreatedAtException(String message) {
        super(message);
    }
}