package com.plema.url_command_service.domain.repository;

import com.plema.url_command_service.domain.aggregate.ShortUrlAggregate;
import com.plema.url_command_service.domain.vo.ShortUrlId;

import java.util.Optional;

public interface ShortUrlRepository {
    void save(ShortUrlAggregate aggregate);
    boolean existsById(ShortUrlId id);
    Optional<ShortUrlAggregate> findById(ShortUrlId id);

    boolean markDeleted(ShortUrlAggregate aggregate);
}
