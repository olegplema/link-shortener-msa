package com.plema.url_query_service.infrastructure.adapter.in.rest;

import com.plema.url_query_service.domain.exception.ShortUrlNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        var problemDetail = problemDetail(HttpStatus.BAD_REQUEST, "Validation failed", "Request body validation failed.", "VALIDATION_FAILED");
        problemDetail.setProperty("errors", fieldErrors(exception));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> handleHandlerMethodValidation(HandlerMethodValidationException exception) {
        var problemDetail = problemDetail(HttpStatus.BAD_REQUEST, "Validation failed", "Request validation failed.", "VALIDATION_FAILED");
        problemDetail.setProperty("errors", parameterErrors(exception));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException exception) {
        var problemDetail = problemDetail(HttpStatus.BAD_REQUEST, "Validation failed", "Request validation failed.", "VALIDATION_FAILED");
        problemDetail.setProperty("errors", constraintViolations(exception));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ProblemDetail> handleMissingRequestHeader(MissingRequestHeaderException exception) {
        var problemDetail = problemDetail(HttpStatus.BAD_REQUEST, "Missing required header", exception.getMessage(), "MISSING_REQUIRED_HEADER");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        var problemDetail = problemDetail(HttpStatus.BAD_REQUEST, "Malformed request body", "Request body is malformed or unreadable.", "MALFORMED_REQUEST_BODY");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        var detail = "Request parameter '%s' has an invalid value.".formatted(exception.getName());
        var problemDetail = problemDetail(HttpStatus.BAD_REQUEST, "Invalid argument", detail, "INVALID_ARGUMENT");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
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

    private List<Map<String, String>> fieldErrors(MethodArgumentNotValidException exception) {
        return exception.getBindingResult().getFieldErrors().stream()
                .map(this::fieldError)
                .toList();
    }

    private List<Map<String, String>> parameterErrors(HandlerMethodValidationException exception) {
        var errors = new ArrayList<Map<String, String>>();
        exception.getParameterValidationResults().forEach(result ->
                result.getResolvableErrors().forEach(error -> {
                    var details = new LinkedHashMap<String, String>();
                    details.put("parameter", result.getMethodParameter().getParameterName());
                    details.put("message", error.getDefaultMessage());
                    errors.add(details);
                })
        );
        return errors;
    }

    private Map<String, String> fieldError(FieldError error) {
        var details = new LinkedHashMap<String, String>();
        details.put("field", error.getField());
        details.put("message", error.getDefaultMessage());
        return details;
    }

    private List<Map<String, String>> constraintViolations(ConstraintViolationException exception) {
        return exception.getConstraintViolations().stream()
                .map(violation -> {
                    var details = new LinkedHashMap<String, String>();
                    details.put("parameter", violation.getPropertyPath().toString());
                    details.put("message", violation.getMessage());
                    return (Map<String, String>) details;
                })
                .toList();
    }
}
