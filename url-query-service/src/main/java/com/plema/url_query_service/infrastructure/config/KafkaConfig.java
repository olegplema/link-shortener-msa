package com.plema.url_query_service.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.time.Duration;

@Configuration
public class KafkaConfig {

    @Bean
    public CommonErrorHandler kafkaErrorHandler(
            @Value("${kafka.consumer.retry.attempts}") int attempts,
            @Value("${kafka.consumer.retry.backoff.delay}") Duration initialInterval,
            @Value("${kafka.consumer.retry.backoff.multiplier}") double multiplier,
            @Value("${kafka.consumer.retry.backoff.max-delay}") Duration maxInterval
    ) {
        var backOff = new ExponentialBackOffWithMaxRetries(Math.max(attempts - 1, 0));
        backOff.setInitialInterval(initialInterval.toMillis());
        backOff.setMultiplier(multiplier);
        backOff.setMaxInterval(maxInterval.toMillis());

        return new DefaultErrorHandler(backOff);
    }
}
