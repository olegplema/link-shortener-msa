package com.plema.infrasturcture.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
public class ShortUrlEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String originalUrl;

    @Column(nullable = false)
    private OffsetDateTime expiration;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
