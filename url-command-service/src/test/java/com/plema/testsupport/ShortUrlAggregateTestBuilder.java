package com.plema.testsupport;

import com.plema.url_command_service.domain.aggregate.ShortUrlAggregate;
import com.plema.url_command_service.domain.vo.CreatedAt;
import com.plema.url_command_service.domain.vo.Expiration;
import com.plema.url_command_service.domain.vo.OriginalUrl;
import com.plema.url_command_service.domain.vo.ShortUrlId;

import java.time.OffsetDateTime;

public final class ShortUrlAggregateTestBuilder {
    public static final String DEFAULT_ID = "abcde1";
    public static final String DEFAULT_URL = "http://example.com";

    private String id = DEFAULT_ID;
    private String url = DEFAULT_URL;
    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime expiration = OffsetDateTime.now().plusDays(1);
    private boolean deleted;
    private OffsetDateTime deletedAt;

    private ShortUrlAggregateTestBuilder() {
    }

    public static ShortUrlAggregateTestBuilder aShortUrl() {
        return new ShortUrlAggregateTestBuilder();
    }

    public ShortUrlAggregateTestBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public ShortUrlAggregateTestBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public ShortUrlAggregateTestBuilder withExpiration(OffsetDateTime expiration) {
        this.expiration = expiration;
        return this;
    }

    public ShortUrlAggregateTestBuilder withCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ShortUrlAggregateTestBuilder deletedAt(OffsetDateTime deletedAt) {
        this.deleted = true;
        this.deletedAt = deletedAt;
        return this;
    }

    public ShortUrlAggregate buildCreated() {
        return ShortUrlAggregate.create(id, url, createdAt);
    }

    public ShortUrlAggregate buildReconstituted() {
        return ShortUrlAggregate.reconstitute(
                new ShortUrlId(id),
                new OriginalUrl(url),
                new Expiration(expiration),
                new CreatedAt(createdAt),
                deleted,
                deletedAt,
                1L
        );
    }
}
