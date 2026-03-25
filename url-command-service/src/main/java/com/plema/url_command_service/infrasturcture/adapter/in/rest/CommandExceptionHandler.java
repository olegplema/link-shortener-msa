package com.plema.url_command_service.infrasturcture.adapter.in.rest;

import com.plema.url_command_service.application.exception.IdempotencyConflictException;
import com.plema.url_command_service.domain.exception.InvalidCreatedAtException;
import com.plema.url_command_service.domain.exception.InvalidExpirationException;
import com.plema.url_command_service.domain.exception.InvalidShortUrlIdException;
import com.plema.url_command_service.domain.exception.InvalidUrlException;
import com.plema.url_command_service.domain.exception.ShortUrlNotFoundException;
import com.plema.url_command_service.domain.exception.UrlIdExistsException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
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
public class CommandExceptionHandler {

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ProblemDetail> handleIdempotencyConflict(IdempotencyConflictException exception) {
        var problemDetail = problemDetail(HttpStatus.CONFLICT, "Idempotency conflict", exception.getMessage(), exception.getCode().name());
        var headers = new HttpHeaders();

        if (exception.getRetryAfterMs() != null) {
            problemDetail.setProperty("retryAfterMs", exception.getRetryAfterMs());
            headers.set(HttpHeaders.RETRY_AFTER, "1");
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).headers(headers).body(problemDetail);
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
