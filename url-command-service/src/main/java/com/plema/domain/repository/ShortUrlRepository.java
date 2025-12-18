package com.plema.domain.repository;

import com.plema.domain.aggregate.ShortUrlAggregate;
import com.plema.domain.vo.ShortUrlId;

public interface ShortUrlRepository {
    void save(ShortUrlAggregate aggregate);
    boolean existsById(ShortUrlId id);
}
