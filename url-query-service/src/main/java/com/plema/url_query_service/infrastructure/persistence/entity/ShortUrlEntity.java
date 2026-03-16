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

    @Column
    private String originalUrl;

    @Column
    private OffsetDateTime expiration;

    @Column(nullable = false)
    private Integer clickCount;

    @Column
    private OffsetDateTime lastAccessedAt;

    @Column
    private OffsetDateTime createdAt;

    @Column(name = "aggregate_version", nullable = false)
    private long aggregateVersion;

    @Column(nullable = false)
    private boolean deleted;
}
