package com.plema.url_query_service.infrastructure.messaging.kafka;

import com.plema.url_query_service.application.port.out.ShortUrlCache;
import com.plema.url_query_service.domain.event.ShortUrlCreatedEvent;
import com.plema.url_query_service.domain.event.ShortUrlDeletedEvent;
import com.plema.url_query_service.domain.model.ShortUrlReadModel;
import com.plema.url_query_service.domain.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@RetryableTopic(
        attempts = "${spring.kafka.retry.topic.attempts}",
        retryTopicSuffix = "${kafka.retry.topic.retry-suffix}",
        dltTopicSuffix = "${kafka.retry.topic.dlt-suffix}",
        autoCreateTopics = "${kafka.retry.topic.auto-create-topics}",
        numPartitions = "${kafka.retry.topic.num-partitions}",
        replicationFactor = "${kafka.retry.topic.replication-factor}",
        backOff = @BackOff(
                delayString = "${spring.kafka.retry.topic.backoff.delay}",
                multiplierString = "${spring.kafka.retry.topic.backoff.multiplier}",
                maxDelayString = "${spring.kafka.retry.topic.backoff.max-delay}"
        )
)
@KafkaListener(topics = "${kafka.topics.short-url-events}")
public class ShortUrlEventConsumer {
    private final ShortUrlRepository queryRepository;
    private final ShortUrlCache cache;

    @KafkaHandler
    public void handleShortUrlCreatedEvent(ShortUrlCreatedEvent event, Acknowledgment ack) {
        IO.println("TEST HANDLER");
        IO.println(event.id());
        IO.println(event.originalUrl());
        IO.println(event.expiration());
        IO.println(event.createdAt());
        var readModel = new ShortUrlReadModel(
                event.id(),
                event.originalUrl(),
                event.expiration(),
                0,
                event.createdAt()
        );

        queryRepository.save(readModel);
        ack.acknowledge();
    }

    @KafkaHandler
    public void handleShortUrlDeletedEvent(ShortUrlDeletedEvent event, Acknowledgment ack) {
        queryRepository.deleteById(event.id());
        cache.evict(event.id());
        ack.acknowledge();
    }
}
