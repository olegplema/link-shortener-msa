package com.plema.url_command_service.infrasturcture.adapter.in.rest;

import com.plema.url_command_service.application.service.IdempotentCreateShortUrlService;
import com.plema.url_command_service.application.service.IdempotentDeleteShortUrlService;
import com.plema.url_command_service.infrasturcture.adapter.in.rest.dto.CreateShortUrlRequest;
import com.plema.url_command_service.infrasturcture.adapter.in.rest.dto.CreateUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class ShortUrlController {

    private final CreateShortUrlRequestHashService createShortUrlRequestHashService;
    private final IdempotentCreateShortUrlService idempotentCreateShortUrlService;
    private final IdempotentDeleteShortUrlService idempotentDeleteShortUrlService;
    private final DeleteShortUrlRequestHashService deleteShortUrlRequestHashService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUrlResponse createUrl(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody CreateShortUrlRequest createShortUrlRequest
    ) {
        var result = idempotentCreateShortUrlService.createShortUrl(
                idempotencyKey,
                createShortUrlRequest.originalUrl(),
                createShortUrlRequestHashService.createHash(createShortUrlRequest)
        );

        return new CreateUrlResponse(result.id(), result.originalUrl(), result.expiration());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUrl(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @PathVariable String id
    ) {
        idempotentDeleteShortUrlService.deleteShortUrl(
                idempotencyKey,
                id,
                deleteShortUrlRequestHashService.createHash(id)
        );
    }
}
