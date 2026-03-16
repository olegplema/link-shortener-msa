package com.plema.url_command_service.application.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plema.url_command_service.application.model.DeleteShortUrlResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteShortUrlResultSnapshotSerializer {

    private final ObjectMapper objectMapper;

    public String serialize(DeleteShortUrlResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize idempotent delete response.", e);
        }
    }
}
