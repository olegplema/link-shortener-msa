package com.plema.url_command_service.application.service;

import com.plema.url_command_service.application.port.out.OutboxRepository;
import com.plema.url_command_service.application.port.out.ShortUrlIdGenerator;
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
    private final ShortUrlIdGenerator shortUrlIdGenerator;
    private final Clock clock;

    @Transactional
    public ShortUrlAggregate createShortUrl(String url) {
        var shortUrl = ShortUrlAggregate.create(shortUrlIdGenerator.nextId(), url, OffsetDateTime.now(clock));

        urlUniquenessChecker.ensureUnique(shortUrl.getId());

        shortUrlRepository.save(shortUrl);
        outboxRepository.saveEvents(shortUrl.getDomainEvents());

        shortUrl.clearDomainEvents();

        return shortUrl;
    }
}
