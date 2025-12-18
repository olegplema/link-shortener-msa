package com.plema.infrasturcture.persistence.adapter;

import com.plema.domain.aggregate.ShortUrlAggregate;
import com.plema.domain.repository.ShortUrlRepository;
import com.plema.domain.vo.ShortUrlId;
import com.plema.infrasturcture.persistence.jpa.SpringDataOutboxRepository;
import com.plema.infrasturcture.persistence.jpa.SpringDataShortUrlRepository;
import com.plema.infrasturcture.persistence.mapper.OutboxMapper;
import com.plema.infrasturcture.persistence.mapper.ShortUrlMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    public boolean existsById(ShortUrlId id) {
        return shortUrlDao.existsById(id.value());
    }
}
