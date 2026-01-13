package com.plema.url_query_service.infrastructure.persistence.adapter;

import com.plema.url_query_service.domain.model.ShortUrlReadModel;
import com.plema.url_query_service.domain.repository.ShortUrlRepository;
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
        return shortUrlQueryRepository.findById(id).map(shortUrlMapper::toReadModel);
    }

    @Override
    @Transactional
    public void save(ShortUrlReadModel readModel) {
        var entity = shortUrlMapper.toEntity(readModel);
        shortUrlQueryRepository.save(entity);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        shortUrlQueryRepository.deleteById(id);
    }
}
