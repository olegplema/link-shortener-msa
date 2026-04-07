package com.plema.url_command_service.infrasturcture.adapter.out.metrics;

import com.plema.url_command_service.application.idempotency.IdempotencyOperation;
import com.plema.url_command_service.application.port.out.IdempotencyMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MeterRegistryIdempotencyMetrics implements IdempotencyMetrics {

    private static final String IDEMPOTENCY_REPLAY_METRIC = "idempotency_replay_total";
    private static final String IDEMPOTENCY_CONFLICT_METRIC = "idempotency_conflict_total";

    private final MeterRegistry meterRegistry;

    public MeterRegistryIdempotencyMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void incrementReplay(IdempotencyOperation operation) {
        meterRegistry.counter(IDEMPOTENCY_REPLAY_METRIC, "operation", operation.name()).increment();
    }

    @Override
    public void incrementConflict(IdempotencyOperation operation, String code) {
        meterRegistry.counter(IDEMPOTENCY_CONFLICT_METRIC, "operation", operation.name(), "code", code).increment();
    }
}
