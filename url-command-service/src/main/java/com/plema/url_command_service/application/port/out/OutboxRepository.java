package com.plema.url_command_service.application.port.out;

import com.plema.url_command_service.domain.event.DomainEvent;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxRepository {
    void saveEvents(List<DomainEvent> events);
}
