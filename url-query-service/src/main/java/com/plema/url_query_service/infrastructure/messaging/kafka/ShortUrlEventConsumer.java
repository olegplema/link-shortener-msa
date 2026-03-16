package com.plema.url_query_service.infrastructure.messaging.kafka;

import com.plema.url_query_service.application.port.out.ShortUrlCache;
import com.plema.url_query_service.domain.event.ShortUrlCreatedEvent;
import com.plema.url_query_service.domain.event.ShortUrlDeletedEvent;
import com.plema.url_query_service.domain.model.ShortUrlReadModel;
import com.plema.url_query_service.domain.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = "${kafka.topics.short-url-events}")
public class ShortUrlEventConsumer {
    private final ShortUrlRepository queryRepository;
    private final ShortUrlCache cache;

    @KafkaHandler
    public void handleShortUrlCreatedEvent(ShortUrlCreatedEvent event, Acknowledgment ack) {
        var readModel = new ShortUrlReadModel(
                event.id(),
                event.originalUrl(),
                event.expiration(),
                0,
                event.createdAt(),
                event.aggregateVersion(),
                false
        );

        queryRepository.applyCreated(readModel);
        ack.acknowledge();
    }

    @KafkaHandler
    public void handleShortUrlDeletedEvent(ShortUrlDeletedEvent event, Acknowledgment ack) {
        queryRepository.applyDeleted(event.id(), event.aggregateVersion(), event.createdAt());
        cache.evict(event.id());
        ack.acknowledge();
    }
}
