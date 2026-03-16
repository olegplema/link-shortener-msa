package com.plema.url_query_service.infrastructure.persistence.mapper;

import com.plema.url_query_service.domain.model.ShortUrlReadModel;
import com.plema.url_query_service.infrastructure.persistence.entity.ShortUrlEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ShortUrlMapper {
    public abstract ShortUrlReadModel toReadModel(ShortUrlEntity entity);

    @Mapping(target = "lastAccessedAt", ignore = true)
    public abstract ShortUrlEntity toEntity(ShortUrlReadModel readModel);
}
