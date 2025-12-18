package com.plema.domain.aggregate;

import com.plema.domain.event.ShortUrlCreatedEvent;
import com.plema.domain.vo.Expiration;
import com.plema.domain.vo.OriginalUrl;
import com.plema.domain.vo.ShortUrlId;
import com.plema.domain.event.DomainEvent;
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

    @Getter(lombok.AccessLevel.NONE)
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private ShortUrlAggregate(ShortUrlId id, OriginalUrl originalUrl, Expiration expiration) {
        this.id = id;
        this.originalUrl = originalUrl;
        this.expiration = expiration;
    }

    public static ShortUrlAggregate create(String rawId, String rawUrl, OffsetDateTime rawExpiration) {
        var id = new ShortUrlId(rawId);
        var originalUrl = new OriginalUrl(rawUrl);
        var expiration = new Expiration(rawExpiration);

        var aggregate = new ShortUrlAggregate(id, originalUrl, expiration);

        aggregate.registerEvent(
                new ShortUrlCreatedEvent(
                        aggregate.id.value(),
                        aggregate.originalUrl.value(),
                        aggregate.expiration.value()
                )
        );

        return aggregate;
    }

    public static ShortUrlAggregate reconstitute(ShortUrlId id, OriginalUrl originalUrl, Expiration expiration) {
        return new ShortUrlAggregate(id, originalUrl, expiration);
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
}
