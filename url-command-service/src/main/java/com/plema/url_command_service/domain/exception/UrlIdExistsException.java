package com.plema.url_command_service.domain.exception;

public class UrlIdExistsException extends RuntimeException {
    public UrlIdExistsException(String message) {
        super(message);
    }
}
