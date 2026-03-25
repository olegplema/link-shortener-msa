package com.plema.url_command_service.infrasturcture.adapter.in.rest;

import com.plema.url_command_service.application.exception.IdempotencyConflictException;
import com.plema.url_command_service.domain.exception.InvalidCreatedAtException;
import com.plema.url_command_service.domain.exception.InvalidExpirationException;
import com.plema.url_command_service.domain.exception.InvalidShortUrlIdException;
import com.plema.url_command_service.domain.exception.InvalidUrlException;
import com.plema.url_command_service.domain.exception.ShortUrlNotFoundException;
import com.plema.url_command_service.domain.exception.UrlIdExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommandExceptionHandler {

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ProblemDetail> handleIdempotencyConflict(IdempotencyConflictException exception) {
        var problemDetail = problemDetail(HttpStatus.CONFLICT, "Idempotency conflict", exception.getMessage(), exception.getCode().name());

        if (exception.getRetryAfterMs() != null) {
            problemDetail.setProperty("retryAfterMs", exception.getRetryAfterMs());
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleShortUrlNotFound(ShortUrlNotFoundException exception) {
        var problemDetail = problemDetail(HttpStatus.NOT_FOUND, "Short URL not found", exception.getMessage(), "SHORT_URL_NOT_FOUND");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler({
            InvalidShortUrlIdException.class,
            InvalidUrlException.class,
            InvalidExpirationException.class,
            InvalidCreatedAtException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ProblemDetail> handleBadRequest(RuntimeException exception) {
        var code = switch (exception) {
            case InvalidShortUrlIdException ignored -> "INVALID_SHORT_URL_ID";
            case InvalidUrlException ignored -> "INVALID_URL";
            case InvalidExpirationException ignored -> "INVALID_EXPIRATION";
            case InvalidCreatedAtException ignored -> "INVALID_CREATED_AT";
            default -> "INVALID_ARGUMENT";
        };

        var problemDetail = problemDetail(HttpStatus.BAD_REQUEST, "Bad request", exception.getMessage(), code);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(UrlIdExistsException.class)
    public ResponseEntity<ProblemDetail> handleUrlIdExists(UrlIdExistsException exception) {
        var problemDetail = problemDetail(HttpStatus.CONFLICT, "Short URL id conflict", exception.getMessage(), "SHORT_URL_ID_EXISTS");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException exception) {
        var problemDetail = problemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", exception.getMessage(), "INTERNAL_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception exception) {
        var problemDetail = problemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "Unexpected server error.", "INTERNAL_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    private ProblemDetail problemDetail(HttpStatus status, String title, String detail, String code) {
        var problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setProperty("code", code);
        return problemDetail;
    }
}
