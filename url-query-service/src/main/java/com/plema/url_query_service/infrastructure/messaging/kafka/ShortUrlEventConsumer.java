package com.plema.url_query_service.infrastructure.messaging.kafka;

import com.plema.url_query_service.application.port.out.ShortUrlCache;
import com.plema.url_query_service.domain.event.ShortUrlCreatedEvent;
import com.plema.url_query_service.domain.event.ShortUrlDeletedEvent;
import com.plema.url_query_service.domain.model.ShortUrlReadModel;
import com.plema.url_query_service.domain.repository.ShortUrlRepository;
import com.plema.url_query_service.infrastructure.support.RequestIdSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = "${kafka.topics.short-url-events}")
public class ShortUrlEventConsumer {
    private final ShortUrlRepository queryRepository;
    private final ShortUrlCache cache;

    @KafkaHandler
    public void handleShortUrlCreatedEvent(
            ShortUrlCreatedEvent event,
            @Header(name = RequestIdSupport.REQUEST_ID_HEADER, required = false) String requestId,
            Acknowledgment ack
    ) {
        processWithRequestId(requestId, () -> {
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
        });
    }

    @KafkaHandler
    public void handleShortUrlDeletedEvent(
            ShortUrlDeletedEvent event,
            @Header(name = RequestIdSupport.REQUEST_ID_HEADER, required = false) String requestId,
            Acknowledgment ack
    ) {
        processWithRequestId(requestId, () -> {
            queryRepository.applyDeleted(event.id(), event.aggregateVersion(), event.createdAt());
            cache.evict(event.id());
            ack.acknowledge();
        });
    }

    private void processWithRequestId(String requestId, Runnable action) {
        RequestIdSupport.bindToMdc(requestId);

        try {
            action.run();
        } finally {
            RequestIdSupport.clearFromMdc();
        }
    }
}
