package com.plema.url_command_service.infrasturcture.adapter.in.rest;

import com.plema.url_command_service.application.service.CreateShortUrlService;
import com.plema.url_command_service.application.service.DeleteShortUrlService;
import com.plema.url_command_service.infrasturcture.adapter.in.rest.dto.CreateShortUrlRequest;
import com.plema.url_command_service.infrasturcture.adapter.in.rest.dto.CreateUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class ShortUrlController {

    private final CreateShortUrlService createShortUrlService;
    private final DeleteShortUrlService deleteShortUrlService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUrlResponse createUrl(@RequestBody CreateShortUrlRequest createShortUrlRequest) {
        var aggregate = createShortUrlService.createShortUrl(createShortUrlRequest.originalUrl());

        return new CreateUrlResponse(
                aggregate.getId().value(),
                aggregate.getOriginalUrl().value(),
                aggregate.getExpiration().value()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUrl(@PathVariable String id) {
        deleteShortUrlService.deleteShortUrl(id);
    }
}
