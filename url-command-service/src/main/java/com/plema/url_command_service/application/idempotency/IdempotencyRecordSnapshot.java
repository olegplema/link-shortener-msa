package com.plema.url_command_service.application.idempotency;

public record IdempotencyRecordSnapshot(
        String idempotencyKey,
        IdempotencyOperation operation,
        String requestHash,
        String responseBody,
        String resourceId
) {
}
