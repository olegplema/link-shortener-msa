package com.plema.url_command_service.infrasturcture.adapter.in.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plema.url_command_service.application.idempotency.DeleteShortUrlFingerprintPayload;
import com.plema.url_command_service.application.idempotency.IdempotencyHashService;
import com.plema.url_command_service.application.idempotency.IdempotencyOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteShortUrlRequestHashService {

    private final IdempotencyHashService idempotencyHashService;
    private final ObjectMapper objectMapper;

    public String createHash(String id) {
        var payload = new DeleteShortUrlFingerprintPayload(id);

        try {
            return idempotencyHashService.hash(
                    IdempotencyOperation.DELETE_SHORT_URL,
                    objectMapper.writeValueAsString(payload)
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize delete short url fingerprint payload.", e);
        }
    }
}
