package com.plema.url_command_service.application.service;

import com.plema.url_command_service.application.port.out.OutboxRepository;
import com.plema.url_command_service.domain.exception.ShortUrlNotFoundException;
import com.plema.url_command_service.domain.repository.ShortUrlRepository;
import com.plema.url_command_service.domain.vo.ShortUrlId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class DeleteShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final OutboxRepository outboxRepository;
    private final Clock clock;

    @Transactional
    public void deleteShortUrl(String id) {
        var shortUrlId = new ShortUrlId(id);

        var aggregate = shortUrlRepository.findById(shortUrlId).orElseThrow(() -> new ShortUrlNotFoundException("Url with id " + id + " not found"));

        aggregate.delete(OffsetDateTime.now(clock));

        shortUrlRepository.delete(aggregate);

        outboxRepository.saveEvents(aggregate.getDomainEvents());
        aggregate.clearDomainEvents();
    }
}
