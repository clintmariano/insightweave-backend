package com.insightweave.mapper;

import com.insightweave.dto.SummaryDto;
import com.insightweave.entity.Summary;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SummaryMapper {
    SummaryDto toDto(Summary entity);
    List<SummaryDto> toDtoList(List<Summary> entities);
}
