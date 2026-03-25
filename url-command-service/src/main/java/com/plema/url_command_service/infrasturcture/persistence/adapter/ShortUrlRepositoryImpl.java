package com.plema.url_command_service.infrasturcture.persistence.adapter;

import com.plema.url_command_service.domain.aggregate.ShortUrlAggregate;
import com.plema.url_command_service.domain.repository.ShortUrlRepository;
import com.plema.url_command_service.domain.vo.ShortUrlId;
import com.plema.url_command_service.infrasturcture.persistence.jpa.SpringDataShortUrlRepository;
import com.plema.url_command_service.infrasturcture.persistence.mapper.ShortUrlMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShortUrlRepositoryImpl implements ShortUrlRepository {
    private final SpringDataShortUrlRepository shortUrlDao;
    private final ShortUrlMapper shortUrlMapper;

    @Override
    @Transactional
    public void save(ShortUrlAggregate aggregate) {
        var entity = shortUrlMapper.toEntity(aggregate);
        shortUrlDao.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(ShortUrlId id) {
        return shortUrlDao.existsById(id.value());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShortUrlAggregate> findById(ShortUrlId id) {
        return shortUrlDao.findById(id.value())
                .map(shortUrlMapper::toAggregate);
    }

    @Override
    @Transactional
    public boolean markDeleted(ShortUrlAggregate aggregate) {
        var newVersion = aggregate.getAggregateVersion();
        var expectedVersion = newVersion - 1;

        return shortUrlDao.markDeleted(
                aggregate.getId().value(),
                expectedVersion,
                newVersion,
                aggregate.getDeletedAt()
        ) > 0;
    }
}
