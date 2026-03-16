package com.plema.url_command_service.application.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plema.url_command_service.application.model.CreateShortUrlResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateShortUrlResultSnapshotSerializer {

    private final ObjectMapper objectMapper;

    public String serialize(CreateShortUrlResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize idempotent response.", e);
        }
    }

    public CreateShortUrlResult deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, CreateShortUrlResult.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize stored idempotent response.", e);
        }
    }
}
