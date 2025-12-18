package com.plema.infrasturcture.persistence.jpa;

import com.plema.infrasturcture.persistence.entity.ShortUrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataShortUrlRepository extends JpaRepository<ShortUrlEntity, String> {
}
