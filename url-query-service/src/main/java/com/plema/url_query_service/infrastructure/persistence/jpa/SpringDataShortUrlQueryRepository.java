package com.plema.url_query_service.infrastructure.persistence.jpa;

import com.plema.url_query_service.infrastructure.persistence.entity.ShortUrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataShortUrlQueryRepository extends JpaRepository<ShortUrlEntity, String> {
    Optional<ShortUrlEntity> findByIdAndDeletedFalse(String id);
}
