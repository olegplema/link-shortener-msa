package com.plema.domain.repository;

import com.plema.domain.aggregate.ShortUrlAggregate;
import com.plema.domain.vo.ShortUrlId;

import java.util.Optional;

public interface ShortUrlRepository {
    void save(ShortUrlAggregate aggregate);
    boolean existsById(ShortUrlId id);
    Optional<ShortUrlAggregate> findById(ShortUrlId id);

    void delete(ShortUrlAggregate aggregate);
}
