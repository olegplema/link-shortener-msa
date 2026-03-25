package com.plema.url_command_service.infrasturcture.adapter.in.rest;

import com.plema.url_command_service.application.service.IdempotentCreateShortUrlService;
import com.plema.url_command_service.application.service.IdempotentDeleteShortUrlService;
import com.plema.url_command_service.infrasturcture.adapter.in.rest.dto.CreateShortUrlRequest;
import com.plema.url_command_service.infrasturcture.adapter.in.rest.dto.CreateUrlResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
@Validated
public class ShortUrlController {

    private static final String IDEMPOTENCY_KEY_PATTERN = "^[A-Za-z0-9._:-]+$";
    private static final String SHORT_URL_ID_PATTERN = "^[A-Za-z0-9_-]{5,32}$";

    private final CreateShortUrlRequestHashService createShortUrlRequestHashService;
    private final IdempotentCreateShortUrlService idempotentCreateShortUrlService;
    private final IdempotentDeleteShortUrlService idempotentDeleteShortUrlService;
    private final DeleteShortUrlRequestHashService deleteShortUrlRequestHashService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUrlResponse createUrl(
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Idempotency-Key must not be blank")
            @Size(max = 255, message = "Idempotency-Key must be at most 255 characters")
            @Pattern(regexp = IDEMPOTENCY_KEY_PATTERN, message = "Idempotency-Key contains unsupported characters")
            String idempotencyKey,
            @Valid @RequestBody CreateShortUrlRequest createShortUrlRequest
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
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Idempotency-Key must not be blank")
            @Size(max = 255, message = "Idempotency-Key must be at most 255 characters")
            @Pattern(regexp = IDEMPOTENCY_KEY_PATTERN, message = "Idempotency-Key contains unsupported characters")
            String idempotencyKey,
            @PathVariable
            @NotBlank(message = "id must not be blank")
            @Pattern(regexp = SHORT_URL_ID_PATTERN, message = "id must match the short url format")
            String id
    ) {
        idempotentDeleteShortUrlService.deleteShortUrl(
                idempotencyKey,
                id,
                deleteShortUrlRequestHashService.createHash(id)
        );
    }
}
