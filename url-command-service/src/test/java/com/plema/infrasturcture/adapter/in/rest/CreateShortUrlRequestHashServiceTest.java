package com.plema.url_command_service.infrasturcture.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plema.url_command_service.application.idempotency.IdempotencyHashService;
import com.plema.url_command_service.infrasturcture.adapter.in.rest.dto.CreateShortUrlRequest;
import com.plema.url_command_service.infrasturcture.adapter.in.rest.mapper.CreateShortUrlFingerprintMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class CreateShortUrlRequestHashServiceTest {

    @Test
    void should_return_same_hash_for_same_request() {
        var objectMapper = new ObjectMapper().findAndRegisterModules();
        var idempotencyHashService = new IdempotencyHashService();
        var hashService = new CreateShortUrlRequestHashService(
                Mappers.getMapper(CreateShortUrlFingerprintMapper.class),
                idempotencyHashService,
                objectMapper
        );
        var request = new CreateShortUrlRequest("https://example.com");

        var firstHash = hashService.createHash(request);
        var secondHash = hashService.createHash(request);

        assertThat(firstHash).isEqualTo(secondHash);
    }

    @Test
    void should_return_different_hash_for_different_requests() {
        var objectMapper = new ObjectMapper().findAndRegisterModules();
        var idempotencyHashService = new IdempotencyHashService();
        var hashService = new CreateShortUrlRequestHashService(
                Mappers.getMapper(CreateShortUrlFingerprintMapper.class),
                idempotencyHashService,
                objectMapper
        );

        var firstHash = hashService.createHash(new CreateShortUrlRequest("https://example.com"));
        var secondHash = hashService.createHash(new CreateShortUrlRequest("https://example.org"));

        assertThat(firstHash).isNotEqualTo(secondHash);
    }
}
