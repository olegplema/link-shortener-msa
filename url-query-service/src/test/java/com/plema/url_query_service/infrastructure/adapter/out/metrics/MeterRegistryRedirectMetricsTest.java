package com.plema.url_query_service.infrastructure.adapter.out.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MeterRegistryRedirectMetricsTest {

    @Test
    void should_increment_redirect_not_found_counter() {
        var meterRegistry = new SimpleMeterRegistry();
        var metrics = new MeterRegistryRedirectMetrics(meterRegistry);

        metrics.incrementRedirectNotFound();

        assertThat(meterRegistry.get("redirect_not_found_total").counter().count()).isEqualTo(1.0);
    }
}
