package com.plema.testsupport;

import com.plema.url_command_service.domain.aggregate.ShortUrlAggregate;
import com.plema.url_command_service.domain.repository.ShortUrlRepository;
import com.plema.url_command_service.domain.vo.ShortUrlId;

import java.util.Optional;

public class StubShortUrlRepository implements ShortUrlRepository {
    private final boolean exists;

    public StubShortUrlRepository(boolean exists) {
        this.exists = exists;
    }

    public void save(ShortUrlAggregate aggregate) {
        throw new UnsupportedOperationException("Not needed for this test.");
    }

    public boolean existsById(ShortUrlId id) {
        return exists;
    }

    public Optional<ShortUrlAggregate> findById(ShortUrlId id) {
        throw new UnsupportedOperationException("Not needed for this test.");
    }

    public boolean markDeleted(ShortUrlAggregate aggregate) {
        throw new UnsupportedOperationException("Not needed for this test.");
    }
}
