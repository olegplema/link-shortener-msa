package com.plema.url_query_service.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "short_urls")
public class ShortUrlEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String originalUrl;

    @Column(nullable = false)
    private OffsetDateTime expiration;

    @Column(nullable = false)
    private Integer clickCount;

    @Column
    private OffsetDateTime lastAccessedAt;

    @Column(nullable = false)
    private OffsetDateTime createdAt;
}
