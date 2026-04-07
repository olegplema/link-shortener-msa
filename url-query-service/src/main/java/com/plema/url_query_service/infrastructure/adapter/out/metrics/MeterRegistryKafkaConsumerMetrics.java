package com.plema.url_query_service.infrastructure.adapter.out.metrics;

import com.plema.url_query_service.application.port.out.KafkaConsumerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MeterRegistryKafkaConsumerMetrics implements KafkaConsumerMetrics {

    private static final String KAFKA_CONSUMER_RETRY_METRIC = "kafka_consumer_retry_total";
    private static final String KAFKA_CONSUMER_FAILURE_METRIC = "kafka_consumer_failure_total";

    private final MeterRegistry meterRegistry;

    public MeterRegistryKafkaConsumerMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void incrementRetry(String eventType) {
        meterRegistry.counter(KAFKA_CONSUMER_RETRY_METRIC, "eventType", eventType).increment();
    }

    @Override
    public void incrementFailure(String eventType) {
        meterRegistry.counter(KAFKA_CONSUMER_FAILURE_METRIC, "eventType", eventType).increment();
    }
}
