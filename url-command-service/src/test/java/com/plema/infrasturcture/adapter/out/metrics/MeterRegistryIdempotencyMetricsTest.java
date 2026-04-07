package com.plema.infrasturcture.adapter.out.metrics;

import com.plema.url_command_service.application.idempotency.IdempotencyOperation;
import com.plema.url_command_service.infrasturcture.adapter.out.metrics.MeterRegistryIdempotencyMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MeterRegistryIdempotencyMetricsTest {

    @Test
    void should_increment_replay_counter() {
        var meterRegistry = new SimpleMeterRegistry();
        var metrics = new MeterRegistryIdempotencyMetrics(meterRegistry);

        metrics.incrementReplay(IdempotencyOperation.CREATE_SHORT_URL);

        assertThat(meterRegistry.get("idempotency_replay_total")
                .tag("operation", IdempotencyOperation.CREATE_SHORT_URL.name())
                .counter()
                .count()).isEqualTo(1.0);
    }

    @Test
    void should_increment_conflict_counter() {
        var meterRegistry = new SimpleMeterRegistry();
        var metrics = new MeterRegistryIdempotencyMetrics(meterRegistry);

        metrics.incrementConflict(IdempotencyOperation.DELETE_SHORT_URL, "REQUEST_IN_PROGRESS");

        assertThat(meterRegistry.get("idempotency_conflict_total")
                .tag("operation", IdempotencyOperation.DELETE_SHORT_URL.name())
                .tag("code", "REQUEST_IN_PROGRESS")
                .counter()
                .count()).isEqualTo(1.0);
    }
}
