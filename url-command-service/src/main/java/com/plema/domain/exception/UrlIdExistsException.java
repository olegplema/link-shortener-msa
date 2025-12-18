package com.plema.domain.exception;

public class UrlIdExistsException extends RuntimeException {
    public UrlIdExistsException(String message) {
        super(message);
    }
}
