package com.plema.url_query_service.infrastructure.persistence.jpa;

import com.plema.url_query_service.infrastructure.persistence.entity.ShortUrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataShortUrlQueryRepository extends JpaRepository<ShortUrlEntity, String> {
}
