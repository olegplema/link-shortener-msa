package com.plema.application.port.out;

import com.plema.domain.event.DomainEvent;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxRepository {
    void saveEvents(List<DomainEvent> events);
}
