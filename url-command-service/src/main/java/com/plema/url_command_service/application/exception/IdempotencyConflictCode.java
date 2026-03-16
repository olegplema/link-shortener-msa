package com.plema.url_command_service.application.exception;

public enum IdempotencyConflictCode {
    REQUEST_IN_PROGRESS,
    IDEMPOTENCY_KEY_PAYLOAD_MISMATCH
}
