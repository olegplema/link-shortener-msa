package com.plema.url_query_service.infrastructure.adapter.in.rest;

import com.plema.url_query_service.application.service.GetShortUrlService;
import com.plema.url_query_service.application.port.out.RedirectMetrics;
import com.plema.url_query_service.domain.exception.ShortUrlNotFoundException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/r")
@RequiredArgsConstructor
@Validated
public class ShortUrlController {

    private static final String SHORT_URL_CODE_PATTERN = "^[A-Za-z0-9_-]{5,32}$";

    private final GetShortUrlService queryService;
    private final RedirectMetrics redirectMetrics;

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(
            @PathVariable
            @NotBlank(message = "code must not be blank")
            @Pattern(regexp = SHORT_URL_CODE_PATTERN, message = "code must match the short url format")
            String code
    ) {
        return queryService.findById(code).<ResponseEntity<Void>>map(
                shortUrlReadModel ->
                        ResponseEntity.status(HttpStatus.FOUND)
                                .location(URI.create(shortUrlReadModel.originalUrl()))
                                .build()
        ).orElseThrow(() -> {
            redirectMetrics.incrementRedirectNotFound();
            return new ShortUrlNotFoundException("Short URL with code " + code + " not found");
        });
    }

}
