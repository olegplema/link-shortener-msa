package com.plema.url_command_service.infrasturcture.adapter.in.rest.mapper;

import com.plema.url_command_service.application.idempotency.CreateShortUrlFingerprintPayload;
import com.plema.url_command_service.infrasturcture.adapter.in.rest.dto.CreateShortUrlRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface CreateShortUrlFingerprintMapper {
    CreateShortUrlFingerprintPayload toPayload(CreateShortUrlRequest request);
}
