package com.plema.infrasturcture.adapter.in.rest;

import com.plema.application.service.CreateShortUrlService;
import com.plema.infrasturcture.adapter.in.rest.dto.CreateShortUrlRequest;
import com.plema.infrasturcture.adapter.in.rest.dto.CreateUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class ShortUrlController {

    private final CreateShortUrlService createShortUrlService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUrlResponse createUrl(@RequestBody CreateShortUrlRequest createShortUrlRequest) {
        var aggregate = createShortUrlService.createShortUrl(createShortUrlRequest.originalUrl(), createShortUrlRequest.expiration());

        return new CreateUrlResponse(
                aggregate.getId().value(),
                aggregate.getOriginalUrl().value(),
                aggregate.getExpiration().value()
        );
    }
}
