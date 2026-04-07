package com.plema.url_command_service.application.port.out;

import com.plema.url_command_service.application.idempotency.IdempotencyOperation;

public interface IdempotencyMetrics {
    void incrementReplay(IdempotencyOperation operation);

    void incrementConflict(IdempotencyOperation operation, String code);
}
