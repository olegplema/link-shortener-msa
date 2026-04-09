package com.plema.infrasturcture.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plema.url_command_service.domain.event.ShortUrlCreatedEvent;
import com.plema.url_command_service.infrasturcture.observability.RequestIdAccessor;
import com.plema.url_command_service.infrasturcture.persistence.mapper.OutboxMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OutboxMapperTest {

    @Test
    void should_map_request_id_to_outbox_entity_without_leaking_it_into_payload() {
        var mapper = Mappers.getMapper(OutboxMapper.class);
        var requestIdAccessor = mock(RequestIdAccessor.class);
        var objectMapper = new ObjectMapper().findAndRegisterModules();
        var createdAt = OffsetDateTime.now();
        var event = new ShortUrlCreatedEvent("abc123", "https://example.com", createdAt.plusDays(7), 1L, createdAt);

        ReflectionTestUtils.setField(mapper, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(mapper, "requestIdAccessor", requestIdAccessor);
        when(requestIdAccessor.currentRequestId()).thenReturn(Optional.of("req-123"));

        var entity = mapper.toEntity(event);

        assertThat(entity.getRequestId()).isEqualTo("req-123");
        assertThat(entity.getPayload()).contains("\"id\":\"abc123\"");
        assertThat(entity.getPayload()).doesNotContain("requestId");
    }
}
