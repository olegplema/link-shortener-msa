package com.plema.testsupport;

import com.plema.domain.aggregate.ShortUrlAggregate;
import com.plema.domain.vo.Expiration;
import com.plema.domain.vo.OriginalUrl;
import com.plema.domain.vo.ShortUrlId;

import java.time.OffsetDateTime;

public final class ShortUrlAggregateTestBuilder {
    public static final String DEFAULT_ID = "abcde1";
    public static final String DEFAULT_URL = "http://example.com";

    private String id = DEFAULT_ID;
    private String url = DEFAULT_URL;
    private OffsetDateTime expiration = OffsetDateTime.now().plusDays(1);

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

    public ShortUrlAggregate buildCreated() {
        return ShortUrlAggregate.create(id, url, expiration);
    }

    public ShortUrlAggregate buildReconstituted() {
        return ShortUrlAggregate.reconstitute(
                new ShortUrlId(id),
                new OriginalUrl(url),
                new Expiration(expiration)
        );
    }
}
