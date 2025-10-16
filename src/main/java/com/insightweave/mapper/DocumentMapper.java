package com.insightweave.mapper;

import com.insightweave.dto.*;
import com.insightweave.entity.Document;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DocumentMapper {

    // Create: request -> entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    Document toEntity(DocumentCreateRequest req);

    // Read: entity -> response
    DocumentResponse toResponse(Document entity);

    // Update existing entity (for PUT/PATCH)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    void updateEntity(@MappingTarget Document target, DocumentCreateRequest req);
}