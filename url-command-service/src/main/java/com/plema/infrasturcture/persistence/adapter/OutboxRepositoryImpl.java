package com.plema.infrasturcture.persistence.adapter;

import com.plema.application.port.out.OutboxRepository;
import com.plema.domain.event.DomainEvent;
import com.plema.infrasturcture.persistence.jpa.SpringDataOutboxRepository;
import com.plema.infrasturcture.persistence.mapper.OutboxMapper;
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
                    .map(event -> outboxMapper.toEntity(event))
                    .toList();

            outboxDao.saveAll(outboxEntities);
        }
    }

}
