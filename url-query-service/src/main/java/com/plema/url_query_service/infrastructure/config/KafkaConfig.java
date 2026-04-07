package com.plema.url_query_service.infrastructure.config;

import com.plema.url_query_service.application.port.out.KafkaConsumerMetrics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.RetryListener;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.time.Duration;

@Configuration
public class KafkaConfig {

    @Bean
    public CommonErrorHandler kafkaErrorHandler(
            KafkaConsumerMetrics kafkaConsumerMetrics,
            @Value("${kafka.consumer.retry.attempts}") int attempts,
            @Value("${kafka.consumer.retry.backoff.delay}") Duration initialInterval,
            @Value("${kafka.consumer.retry.backoff.multiplier}") double multiplier,
            @Value("${kafka.consumer.retry.backoff.max-delay}") Duration maxInterval
    ) {
        var backOff = new ExponentialBackOffWithMaxRetries(Math.max(attempts - 1, 0));
        backOff.setInitialInterval(initialInterval.toMillis());
        backOff.setMultiplier(multiplier);
        backOff.setMaxInterval(maxInterval.toMillis());

        var errorHandler = new DefaultErrorHandler((record, exception) -> {
            kafkaConsumerMetrics.incrementFailure(eventType(record));
            throw new RuntimeException(exception);
        }, backOff);
        errorHandler.setRetryListeners(retryListener(kafkaConsumerMetrics));
        return errorHandler;
    }

    private RetryListener retryListener(KafkaConsumerMetrics kafkaConsumerMetrics) {
        return new RetryListener() {
            @Override
            public void failedDelivery(ConsumerRecord<?, ?> record, Exception ex, int deliveryAttempt) {
                if (deliveryAttempt > 1) {
                    kafkaConsumerMetrics.incrementRetry(eventType(record));
                }
            }
        };
    }

    private String eventType(ConsumerRecord<?, ?> record) {
        return record.value() == null ? "unknown" : record.value().getClass().getSimpleName();
    }
}
