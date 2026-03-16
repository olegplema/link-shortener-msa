package com.plema.url_command_service.domain.aggregate;

import com.plema.url_command_service.domain.event.ShortUrlCreatedEvent;
import com.plema.url_command_service.domain.event.ShortUrlDeletedEvent;
import com.plema.url_command_service.domain.vo.CreatedAt;
import com.plema.url_command_service.domain.vo.Expiration;
import com.plema.url_command_service.domain.vo.OriginalUrl;
import com.plema.url_command_service.domain.vo.ShortUrlId;
import com.plema.url_command_service.domain.event.DomainEvent;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class ShortUrlAggregate {

    private final ShortUrlId id;
    private final OriginalUrl originalUrl;
    private final Expiration expiration;
    private final CreatedAt createdAt;
    private long aggregateVersion;

    private static final int DEFAULT_EXPIRATION_DAYS = 7;

    @Getter(lombok.AccessLevel.NONE)
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private ShortUrlAggregate(
            ShortUrlId id,
            OriginalUrl originalUrl,
            Expiration expiration,
            CreatedAt createdAt,
            long aggregateVersion
    ) {
        this.id = id;
        this.originalUrl = originalUrl;
        this.expiration = expiration;
        this.createdAt = createdAt;
        this.aggregateVersion = aggregateVersion;
    }

    public static ShortUrlAggregate create(String rawId, String rawUrl, OffsetDateTime now) {
        var id = new ShortUrlId(rawId);
        var originalUrl = new OriginalUrl(rawUrl);
        var createdAt = new CreatedAt(now);

        var calculatedExpirationTime = now.plusDays(DEFAULT_EXPIRATION_DAYS);
        var expiration = new Expiration(calculatedExpirationTime);

        var aggregate = new ShortUrlAggregate(id, originalUrl, expiration, createdAt, 0L);
        var eventVersion = aggregate.incrementVersion();

        aggregate.registerEvent(
                new ShortUrlCreatedEvent(
                        aggregate.id.value(),
                        aggregate.originalUrl.value(),
                        aggregate.expiration.value(),
                        eventVersion,
                        aggregate.createdAt.value()
                )
        );

        return aggregate;
    }

    public static ShortUrlAggregate reconstitute(
            ShortUrlId id,
            OriginalUrl originalUrl,
            Expiration expiration,
            CreatedAt createdAt,
            long aggregateVersion
    ) {
        return new ShortUrlAggregate(id, originalUrl, expiration, createdAt, aggregateVersion);
    }

    public void delete(OffsetDateTime dateTime) {
        var eventVersion = incrementVersion();
        registerEvent(new ShortUrlDeletedEvent(id.value(), eventVersion, dateTime));
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    private void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    private long incrementVersion() {
        aggregateVersion += 1;
        return aggregateVersion;
    }
}
