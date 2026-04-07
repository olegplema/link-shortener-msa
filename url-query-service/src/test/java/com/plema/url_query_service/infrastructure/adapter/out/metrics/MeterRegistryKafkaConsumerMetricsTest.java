package com.plema.url_query_service.infrastructure.adapter.out.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MeterRegistryKafkaConsumerMetricsTest {

    @Test
    void should_increment_retry_counter() {
        var meterRegistry = new SimpleMeterRegistry();
        var metrics = new MeterRegistryKafkaConsumerMetrics(meterRegistry);

        metrics.incrementRetry("ShortUrlCreatedEvent");

        assertThat(meterRegistry.get("kafka_consumer_retry_total")
                .tag("eventType", "ShortUrlCreatedEvent")
                .counter()
                .count()).isEqualTo(1.0);
    }

    @Test
    void should_increment_failure_counter() {
        var meterRegistry = new SimpleMeterRegistry();
        var metrics = new MeterRegistryKafkaConsumerMetrics(meterRegistry);

        metrics.incrementFailure("ShortUrlDeletedEvent");

        assertThat(meterRegistry.get("kafka_consumer_failure_total")
                .tag("eventType", "ShortUrlDeletedEvent")
                .counter()
                .count()).isEqualTo(1.0);
    }
}
