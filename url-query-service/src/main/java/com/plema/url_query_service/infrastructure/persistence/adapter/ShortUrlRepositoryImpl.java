package com.plema.url_query_service.infrastructure.persistence.adapter;

import com.plema.url_query_service.domain.model.ShortUrlReadModel;
import com.plema.url_query_service.domain.repository.ShortUrlRepository;
import com.plema.url_query_service.infrastructure.persistence.entity.ShortUrlEntity;
import com.plema.url_query_service.infrastructure.persistence.jpa.SpringDataShortUrlQueryRepository;
import com.plema.url_query_service.infrastructure.persistence.mapper.ShortUrlMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShortUrlRepositoryImpl implements ShortUrlRepository {

    private final SpringDataShortUrlQueryRepository shortUrlQueryRepository;
    private final ShortUrlMapper shortUrlMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<ShortUrlReadModel> findById(String id) {
        return shortUrlQueryRepository.findByIdAndDeletedFalse(id).map(shortUrlMapper::toReadModel);
    }

    @Override
    @Transactional
    public void applyCreated(ShortUrlReadModel readModel) {
        var existingEntity = shortUrlQueryRepository.findById(readModel.id()).orElse(null);

        if (existingEntity != null && existingEntity.getAggregateVersion() >= readModel.aggregateVersion()) {
            return;
        }

        var entity = existingEntity != null ? existingEntity : shortUrlMapper.toEntity(readModel);
        entity.setId(readModel.id()); // TODO Must be a mapper
        entity.setOriginalUrl(readModel.originalUrl());
        entity.setExpiration(readModel.expiration());
        entity.setClickCount(readModel.clickCount());
        entity.setCreatedAt(readModel.createdAt());
        entity.setAggregateVersion(readModel.aggregateVersion());
        entity.setDeleted(false);

        shortUrlQueryRepository.save(entity);
    }

    @Override
    @Transactional
    public void applyDeleted(String id, long aggregateVersion, java.time.OffsetDateTime eventCreatedAt) {
        var entity = shortUrlQueryRepository.findById(id).orElseGet(() -> {
            var tombstone = new ShortUrlEntity();
            tombstone.setId(id);
            tombstone.setClickCount(0);
            tombstone.setCreatedAt(eventCreatedAt);
            return tombstone;
        });

        if (entity.getAggregateVersion() >= aggregateVersion) {
            return;
        }

        entity.setDeleted(true);
        entity.setAggregateVersion(aggregateVersion);

        shortUrlQueryRepository.save(entity);
    }
}
