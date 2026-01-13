package com.plema.domain.exception;

public class InvalidCreatedAtException extends RuntimeException {
    public InvalidCreatedAtException(String message) {
        super(message);
    }
}