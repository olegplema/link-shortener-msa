package com.plema.url_query_service.infrastructure.messaging.kafka;

import com.plema.url_query_service.application.port.out.ShortUrlCache;
import com.plema.url_query_service.domain.event.ShortUrlCreatedEvent;
import com.plema.url_query_service.domain.event.ShortUrlDeletedEvent;
import com.plema.url_query_service.domain.repository.ShortUrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.kafka.support.Acknowledgment;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ShortUrlEventConsumerTest {

    @Mock
    private ShortUrlRepository queryRepository;

    @Mock
    private ShortUrlCache cache;

    @Mock
    private Acknowledgment acknowledgment;

    @Test
    void should_put_valid_request_id_in_mdc_while_processing_created_event() {
        var consumer = new ShortUrlEventConsumer(queryRepository, cache);
        var createdAt = OffsetDateTime.now();
        var event = new ShortUrlCreatedEvent("abc123", "https://example.com", createdAt.plusDays(1), 1L, createdAt);

        doAnswer(invocation -> {
            assertThat(MDC.get("requestId")).isEqualTo("req-123");
            return null;
        }).when(queryRepository).applyCreated(any());

        consumer.handleShortUrlCreatedEvent(event, "req-123", acknowledgment);

        assertThat(MDC.get("requestId")).isNull();
        verify(queryRepository).applyCreated(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void should_ignore_invalid_request_id_header() {
        var consumer = new ShortUrlEventConsumer(queryRepository, cache);
        var event = new ShortUrlDeletedEvent("abc123", 2L, OffsetDateTime.now());

        doAnswer(invocation -> {
            assertThat(MDC.get("requestId")).isNull();
            return null;
        }).when(queryRepository).applyDeleted(any(), any(Long.class), any());

        consumer.handleShortUrlDeletedEvent(event, "bad id with spaces", acknowledgment);

        assertThat(MDC.get("requestId")).isNull();
        verify(queryRepository).applyDeleted(event.id(), event.aggregateVersion(), event.createdAt());
        verify(cache).evict(event.id());
        verify(acknowledgment).acknowledge();
    }
}
