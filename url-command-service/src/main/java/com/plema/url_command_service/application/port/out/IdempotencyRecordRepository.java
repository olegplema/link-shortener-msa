package com.plema.url_command_service.application.port.out;

import com.plema.url_command_service.application.idempotency.IdempotencyOperation;
import com.plema.url_command_service.application.idempotency.IdempotencyRecordSnapshot;

import java.util.Optional;

public interface IdempotencyRecordRepository {
    Optional<IdempotencyRecordSnapshot> findActiveByOperationAndKey(
            IdempotencyOperation operation,
            String idempotencyKey
    );
    boolean trySave(String idempotencyKey, IdempotencyOperation operation, String requestHash);
    void deleteExpiredForKey(IdempotencyOperation operation, String idempotencyKey);
    void updateResponse(
            String idempotencyKey,
            IdempotencyOperation operation,
            String responseBody,
            String resourceId
    );
}
