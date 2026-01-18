package com.plema.url_command_service.infrasturcture.persistence.adapter;

import com.plema.url_command_service.application.port.out.OutboxRepository;
import com.plema.url_command_service.domain.event.DomainEvent;
import com.plema.url_command_service.infrasturcture.persistence.jpa.SpringDataOutboxRepository;
import com.plema.url_command_service.infrasturcture.persistence.mapper.OutboxMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OutboxRepositoryImpl implements OutboxRepository {

    private final OutboxMapper outboxMapper;
    private final SpringDataOutboxRepository outboxDao;

    @Override
    public void saveEvents(List<DomainEvent> events) {
        if (events != null && !events.isEmpty()) {
            var outboxEntities = events.stream()
                    .map(outboxMapper::toEntity)
                    .toList();

            outboxDao.saveAll(outboxEntities);
        }
    }
}
