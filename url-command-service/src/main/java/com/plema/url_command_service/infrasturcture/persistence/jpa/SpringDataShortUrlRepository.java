package com.plema.url_command_service.infrasturcture.persistence.jpa;

import com.plema.url_command_service.infrasturcture.persistence.entity.ShortUrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface SpringDataShortUrlRepository extends JpaRepository<ShortUrlEntity, String> {
    @Modifying
    @Query("""
            update ShortUrlEntity shortUrl
            set shortUrl.deleted = true,
                shortUrl.deletedAt = :deletedAt,
                shortUrl.version = :newVersion
            where shortUrl.id = :id
              and shortUrl.version = :expectedVersion
              and shortUrl.deleted = false
            """)
    int markDeleted(
            @Param("id") String id,
            @Param("expectedVersion") long expectedVersion,
            @Param("newVersion") long newVersion,
            @Param("deletedAt") OffsetDateTime deletedAt
    );
}
