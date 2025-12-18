package com.plema.application.service;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.plema.application.port.out.OutboxRepository;
import com.plema.domain.aggregate.ShortUrlAggregate;
import com.plema.domain.repository.ShortUrlRepository;
import com.plema.domain.service.UrlUniquenessChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class CreateShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final UrlUniquenessChecker urlUniquenessChecker;
    private final OutboxRepository outboxRepository;

    @Transactional
    public ShortUrlAggregate createShortUrl(String url, OffsetDateTime expiration) {
        var nanoId = NanoIdUtils.randomNanoId();
        var shortUrl = ShortUrlAggregate.create(nanoId, url, expiration);

        urlUniquenessChecker.ensureUnique(shortUrl.getId());

        shortUrlRepository.save(shortUrl);
        outboxRepository.saveEvents(shortUrl.getDomainEvents());

        return shortUrl;
    }
}