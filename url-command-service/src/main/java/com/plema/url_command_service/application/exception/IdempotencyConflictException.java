package com.plema.url_command_service.application.exception;

import lombok.Getter;

@Getter
public class IdempotencyConflictException extends RuntimeException {
    private final IdempotencyConflictCode code;
    private final Long retryAfterMs;

    private IdempotencyConflictException(String message, IdempotencyConflictCode code, Long retryAfterMs) {
        super(message);
        this.code = code;
        this.retryAfterMs = retryAfterMs;
    }

    public static IdempotencyConflictException requestInProgress(long retryAfterMs) {
        return new IdempotencyConflictException(
                "Idempotency-Key is already being processed.",
                IdempotencyConflictCode.REQUEST_IN_PROGRESS,
                retryAfterMs
        );
    }

    public static IdempotencyConflictException payloadMismatch() {
        return new IdempotencyConflictException(
                "Idempotency-Key has already been used with a different request.",
                IdempotencyConflictCode.IDEMPOTENCY_KEY_PAYLOAD_MISMATCH,
                null
        );
    }

}
