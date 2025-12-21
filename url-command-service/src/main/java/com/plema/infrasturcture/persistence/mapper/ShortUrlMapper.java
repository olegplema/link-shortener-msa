package com.plema.infrasturcture.persistence.mapper;

import com.plema.domain.aggregate.ShortUrlAggregate;
import com.plema.domain.vo.Expiration;
import com.plema.domain.vo.OriginalUrl;
import com.plema.domain.vo.ShortUrlId;
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


    public ShortUrlAggregate toAggregate(ShortUrlEntity entity) {
        if (entity == null) {
            return null;
        }

        return ShortUrlAggregate.reconstitute(
                new ShortUrlId(entity.getId()),
                new OriginalUrl(entity.getOriginalUrl()),
                new Expiration(entity.getExpiration())
        );
    }
}
