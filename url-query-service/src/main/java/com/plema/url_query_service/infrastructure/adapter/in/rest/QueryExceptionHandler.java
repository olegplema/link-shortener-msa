package com.plema.url_query_service.infrastructure.adapter.in.rest;

import com.plema.url_query_service.domain.exception.ShortUrlNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class QueryExceptionHandler {

    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleShortUrlNotFound(ShortUrlNotFoundException exception) {
        var problemDetail = problemDetail(
                HttpStatus.NOT_FOUND,
                "Short URL not found",
                exception.getMessage(),
                "SHORT_URL_NOT_FOUND"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleInvalidRedirectTarget(IllegalArgumentException exception) {
        var problemDetail = problemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                exception.getMessage(),
                "INVALID_REDIRECT_TARGET"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException exception) {
        var problemDetail = problemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                exception.getMessage(),
                "INTERNAL_ERROR"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception exception) {
        var problemDetail = problemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "Unexpected server error.",
                "INTERNAL_ERROR"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    private ProblemDetail problemDetail(HttpStatus status, String title, String detail, String code) {
        var problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setProperty("code", code);
        return problemDetail;
    }
}
