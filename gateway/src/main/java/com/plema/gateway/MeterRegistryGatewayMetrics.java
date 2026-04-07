package com.plema.gateway;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MeterRegistryGatewayMetrics implements GatewayMetrics {

    private static final String FALLBACK_METRIC = "gateway_fallback_total";

    private final MeterRegistry meterRegistry;

    public MeterRegistryGatewayMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void incrementFallback(String service) {
        meterRegistry.counter(FALLBACK_METRIC, "service", service).increment();
    }
}
