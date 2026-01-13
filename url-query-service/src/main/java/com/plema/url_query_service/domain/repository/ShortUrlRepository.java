package com.plema.url_query_service.domain.repository;

import com.plema.url_query_service.domain.model.ShortUrlReadModel;

import java.util.Optional;

public interface ShortUrlRepository {
    Optional<ShortUrlReadModel> findById(String id);
    void save(ShortUrlReadModel readModel);
    void deleteById(String id);
}
