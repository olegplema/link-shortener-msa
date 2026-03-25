package com.plema.url_command_service.infrasturcture.persistence.entity;

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

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private boolean deleted;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "version", nullable = false)
    private long version;
}
