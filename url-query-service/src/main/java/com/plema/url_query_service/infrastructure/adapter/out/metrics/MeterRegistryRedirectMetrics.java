package com.plema.url_query_service.infrastructure.adapter.out.metrics;

import com.plema.url_query_service.application.port.out.RedirectMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MeterRegistryRedirectMetrics implements RedirectMetrics {

    private static final String REDIRECT_NOT_FOUND_METRIC = "redirect_not_found_total";

    private final MeterRegistry meterRegistry;

    public MeterRegistryRedirectMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void incrementRedirectNotFound() {
        meterRegistry.counter(REDIRECT_NOT_FOUND_METRIC).increment();
    }
}
