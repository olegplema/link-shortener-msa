package com.plema.url_command_service.infrasturcture.persistence.mapper;

import tools.jackson.databind.ObjectMapper;
import com.plema.url_command_service.domain.event.DomainEvent;
import com.plema.url_command_service.infrasturcture.config.EventTypeRegistry;
import com.plema.url_command_service.infrasturcture.persistence.entity.OutboxEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, imports = {EventTypeRegistry.class})
public abstract class OutboxMapper {

    @Autowired
    private ObjectMapper objectMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "aggregateType", expression = "java(EventTypeRegistry.resolveAggregateType(event))")
    @Mapping(target = "aggregateId", expression = "java(event.id())")
    @Mapping(target = "eventType", expression = "java(EventTypeRegistry.resolveEventType(event))")
    @Mapping(target = "payload", source = "event", qualifiedByName = "eventToJson")
    @Mapping(target = "createdAt", ignore = true)
    public abstract OutboxEntity toEntity(DomainEvent event);

    @Named("eventToJson")
    public String eventToJson(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Throwable t) {
            throw new RuntimeException("Error serializing domain event to JSON", t);
        }
    }
}
