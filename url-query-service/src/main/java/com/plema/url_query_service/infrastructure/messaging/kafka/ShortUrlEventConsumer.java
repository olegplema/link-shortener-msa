package com.plema.url_query_service.infrastructure.messaging.kafka;

import com.plema.url_query_service.application.port.out.ShortUrlCache;
import com.plema.url_query_service.domain.event.ShortUrlCreatedEvent;
import com.plema.url_query_service.domain.event.ShortUrlDeletedEvent;
import com.plema.url_query_service.domain.model.ShortUrlReadModel;
import com.plema.url_query_service.domain.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShortUrlEventConsumer {
    private final ShortUrlRepository queryRepository;
    private final ShortUrlCache cache;

    @KafkaListener(
            topics = "${kafka.topics.short-url-events}",
            groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleShortUrlCreatedEvent(ShortUrlCreatedEvent event, Acknowledgment ack) {
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

    @KafkaListener(
            topics = "${kafka.topics.short-url-events}",
            groupId = "${kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleShortUrlDeletedEvent(ShortUrlDeletedEvent event, Acknowledgment ack) {
        queryRepository.deleteById(event.id());
        cache.evict(event.id());
        ack.acknowledge();
    }
}
