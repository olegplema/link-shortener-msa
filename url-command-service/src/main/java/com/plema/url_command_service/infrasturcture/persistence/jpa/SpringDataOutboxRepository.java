package com.plema.url_command_service.infrasturcture.persistence.jpa;

import com.plema.url_command_service.infrasturcture.persistence.entity.OutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataOutboxRepository extends JpaRepository<OutboxEntity, Long> {
}
