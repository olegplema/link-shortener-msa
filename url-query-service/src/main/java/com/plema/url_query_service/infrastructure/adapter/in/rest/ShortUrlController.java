package com.plema.url_query_service.infrastructure.adapter.in.rest;

import com.plema.url_query_service.application.service.GetShortUrlService;
import com.plema.url_query_service.domain.exception.ShortUrlNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/r")
@RequiredArgsConstructor
public class ShortUrlController {

    private final GetShortUrlService queryService;

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        return queryService.findById(code).<ResponseEntity<Void>>map(
                shortUrlReadModel ->
                        ResponseEntity.status(HttpStatus.FOUND)
                                .location(URI.create(shortUrlReadModel.originalUrl()))
                                .build()
        ).orElseThrow(() -> new ShortUrlNotFoundException("Short URL with code " + code + " not found"));
    }

}
