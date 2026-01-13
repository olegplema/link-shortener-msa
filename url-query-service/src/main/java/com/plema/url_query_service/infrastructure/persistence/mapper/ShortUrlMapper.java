package com.plema.url_query_service.infrastructure.persistence.mapper;

import com.plema.url_query_service.domain.model.ShortUrlReadModel;
import com.plema.url_query_service.infrastructure.persistence.entity.ShortUrlEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ShortUrlMapper {
    public abstract ShortUrlReadModel toReadModel(ShortUrlEntity entity);
    public abstract ShortUrlEntity toEntity(ShortUrlReadModel readModel);
}
