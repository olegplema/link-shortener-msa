package com.plema.application.service;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.plema.application.port.out.OutboxRepository;
import com.plema.domain.aggregate.ShortUrlAggregate;
import com.plema.domain.exception.ShortUrlNotFoundException;
import com.plema.domain.repository.ShortUrlRepository;
import com.plema.domain.service.UrlUniquenessChecker;
import com.plema.domain.vo.ShortUrlId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class DeleteShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final OutboxRepository outboxRepository;

    @Transactional
    public void deleteShortUrl(String id) {
        var shortUrlId = new ShortUrlId(id);

        var aggregate = shortUrlRepository.findById(shortUrlId).orElseThrow(() -> new ShortUrlNotFoundException("Url with id " + id + " not found"));

        aggregate.delete();

        shortUrlRepository.delete(aggregate);

        outboxRepository.saveEvents(aggregate.getDomainEvents());
        aggregate.clearDomainEvents();
    }
}
