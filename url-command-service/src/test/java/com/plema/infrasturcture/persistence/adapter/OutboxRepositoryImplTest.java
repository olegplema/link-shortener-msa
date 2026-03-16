package com.plema.infrasturcture.persistence.adapter;

import com.plema.url_command_service.domain.event.DomainEvent;
import com.plema.url_command_service.domain.event.ShortUrlCreatedEvent;
import com.plema.url_command_service.domain.event.ShortUrlDeletedEvent;
import com.plema.url_command_service.infrasturcture.persistence.adapter.OutboxRepositoryImpl;
import com.plema.url_command_service.infrasturcture.persistence.entity.OutboxEntity;
import com.plema.url_command_service.infrasturcture.persistence.jpa.SpringDataOutboxRepository;
import com.plema.url_command_service.infrasturcture.persistence.mapper.OutboxMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxRepositoryImplTest {

    @Mock
    private OutboxMapper outboxMapper;

    @Mock
    private SpringDataOutboxRepository outboxDao;

    @InjectMocks
    private OutboxRepositoryImpl outboxRepository;

    @Test
    void should_save_outbox_entities_when_events_present() {
        var createdAt = OffsetDateTime.now();
        var expiration = createdAt.plusDays(1);
        var createdEvent = new ShortUrlCreatedEvent("abcde1", "http://example.com", expiration, 1L, createdAt);
        var deletedEvent = new ShortUrlDeletedEvent("abcde2", 2L, createdAt);
        var createdEntity = new OutboxEntity();
        createdEntity.setEventType("created.v1");
        createdEntity.setPayload("{\"id\":\"abcde1\"}");
        var deletedEntity = new OutboxEntity();
        deletedEntity.setEventType("deleted.v1");
        deletedEntity.setPayload("{\"id\":\"abcde2\"}");

        when(outboxMapper.toEntity(createdEvent)).thenReturn(createdEntity);
        when(outboxMapper.toEntity(deletedEvent)).thenReturn(deletedEntity);

        outboxRepository.saveEvents(List.of(createdEvent, deletedEvent));

        verify(outboxMapper).toEntity(createdEvent);
        verify(outboxMapper).toEntity(deletedEvent);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OutboxEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(outboxDao).saveAll(captor.capture());
        assertThat(captor.getValue()).containsExactly(createdEntity, deletedEntity);
    }

    @ParameterizedTest
    @MethodSource("emptyEventLists")
    void should_not_save_outbox_entities_when_no_events(List<DomainEvent> events) {
        outboxRepository.saveEvents(events);

        verifyNoInteractions(outboxMapper, outboxDao);
    }

    private static Stream<Arguments> emptyEventLists() {
        return Stream.of(
                Arguments.of((List<DomainEvent>) null),
                Arguments.of(List.of())
        );
    }
}
