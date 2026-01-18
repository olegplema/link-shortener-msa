package com.plema.url_command_service.application.service;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.plema.url_command_service.application.port.out.OutboxRepository;
import com.plema.url_command_service.domain.aggregate.ShortUrlAggregate;
import com.plema.url_command_service.domain.repository.ShortUrlRepository;
import com.plema.url_command_service.domain.service.UrlUniquenessChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Clock;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class CreateShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final UrlUniquenessChecker urlUniquenessChecker;
    private final OutboxRepository outboxRepository;
    private final Clock clock;

    @Transactional
    public ShortUrlAggregate createShortUrl(String url) {
        var nanoId = NanoIdUtils.randomNanoId();
        var shortUrl = ShortUrlAggregate.create(nanoId, url, OffsetDateTime.now(clock));

        urlUniquenessChecker.ensureUnique(shortUrl.getId());

        shortUrlRepository.save(shortUrl);
        outboxRepository.saveEvents(shortUrl.getDomainEvents());

        shortUrl.clearDomainEvents();

        return shortUrl;
    }
}