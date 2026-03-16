package com.plema.url_command_service.infrasturcture.persistence.mapper;

import com.plema.url_command_service.application.idempotency.IdempotencyRecordSnapshot;
import com.plema.url_command_service.infrasturcture.persistence.entity.IdempotencyRecordEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IdempotencyRecordMapper {
    IdempotencyRecordSnapshot toSnapshot(IdempotencyRecordEntity entity);
}
