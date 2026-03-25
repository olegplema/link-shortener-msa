package com.plema.url_command_service.infrasturcture.adapter.in.rest;

import com.plema.url_command_service.application.exception.IdempotencyConflictException;
import com.plema.url_command_service.domain.exception.ShortUrlNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommandExceptionHandler {

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ProblemDetail> handleIdempotencyConflict(IdempotencyConflictException exception) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("Idempotency conflict");
        problemDetail.setProperty("code", exception.getCode().name());

        if (exception.getRetryAfterMs() != null) {
            problemDetail.setProperty("retryAfterMs", exception.getRetryAfterMs());
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleShortUrlNotFound(ShortUrlNotFoundException exception) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("Short URL not found");
        problemDetail.setProperty("code", "SHORT_URL_NOT_FOUND");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }
}
