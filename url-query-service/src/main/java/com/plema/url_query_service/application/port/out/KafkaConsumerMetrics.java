package com.plema.url_query_service.application.port.out;

public interface KafkaConsumerMetrics {
    void incrementRetry(String eventType);

    void incrementFailure(String eventType);
}
