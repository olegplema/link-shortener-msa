package com.plema.domain.exception;

public class InvalidExpirationException extends RuntimeException {
    public InvalidExpirationException(String message) {
        super(message);
    }
}
