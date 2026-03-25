package com.plema.gateway;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
class GatewayExceptionHandler {

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ProblemDetail> handleHandlerMethodValidation(HandlerMethodValidationException exception) {
        var problemDetail = problemDetail(HttpStatus.BAD_REQUEST, "Validation failed", "Request validation failed.", "VALIDATION_FAILED");
        problemDetail.setProperty("errors", parameterErrors(exception));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException exception) {
        var problemDetail = problemDetail(HttpStatus.BAD_REQUEST, "Validation failed", "Request validation failed.", "VALIDATION_FAILED");
        problemDetail.setProperty("errors", exception.getConstraintViolations().stream().map(violation -> {
            var details = new LinkedHashMap<String, String>();
            details.put("parameter", violation.getPropertyPath().toString());
            details.put("message", violation.getMessage());
            return details;
        }).toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleUnexpected(Exception exception) {
        var problemDetail = problemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "Unexpected server error.", "INTERNAL_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    private ProblemDetail problemDetail(HttpStatus status, String title, String detail, String code) {
        var problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setProperty("code", code);
        return problemDetail;
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
}
