package com.plema.url_command_service.infrasturcture.adapter.in.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plema.url_command_service.application.idempotency.IdempotencyHashService;
import com.plema.url_command_service.application.idempotency.IdempotencyOperation;
import com.plema.url_command_service.infrasturcture.adapter.in.rest.dto.CreateShortUrlRequest;
import com.plema.url_command_service.infrasturcture.adapter.in.rest.mapper.CreateShortUrlFingerprintMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateShortUrlRequestHashService {

    private final CreateShortUrlFingerprintMapper createShortUrlFingerprintMapper;
    private final IdempotencyHashService idempotencyHashService;
    private final ObjectMapper objectMapper;

    public String createHash(CreateShortUrlRequest request) {
        var payload = createShortUrlFingerprintMapper.toPayload(request);

        try {
            return idempotencyHashService.hash(
                    IdempotencyOperation.CREATE_SHORT_URL,
                    objectMapper.writeValueAsString(payload)
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize create short url fingerprint payload.", e);
        }
    }
}
