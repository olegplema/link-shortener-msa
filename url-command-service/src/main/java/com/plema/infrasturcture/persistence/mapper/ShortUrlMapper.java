package com.plema.infrasturcture.persistence.mapper;

import com.plema.domain.aggregate.ShortUrlAggregate;
import com.plema.infrasturcture.persistence.entity.ShortUrlEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ShortUrlMapper {

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "originalUrl", source = "originalUrl.value")
    @Mapping(target = "expiration", source = "expiration.value")
    @Mapping(target = "createdAt", ignore = true)
    public abstract ShortUrlEntity toEntity(ShortUrlAggregate aggregate);
}
