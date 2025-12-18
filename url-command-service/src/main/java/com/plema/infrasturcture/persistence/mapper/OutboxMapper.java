package com.plema.infrasturcture.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plema.domain.event.DomainEvent;
import com.plema.infrasturcture.config.EventTypeRegistry;
import com.plema.infrasturcture.persistence.entity.OutboxEntity;
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
    @Mapping(target = "type", expression = "java(EventTypeRegistry.resolveType(event))")
    @Mapping(target = "payload", source = "event", qualifiedByName = "eventToJson")
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
