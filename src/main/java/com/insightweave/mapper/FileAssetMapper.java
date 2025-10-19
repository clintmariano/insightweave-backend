// src/main/java/com/insightweave/mapper/FileAssetMapper.java
package com.insightweave.mapper;

import com.insightweave.dto.FileAssetDto;
import com.insightweave.entity.FileAsset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FileAssetMapper {

    @Mapping(target = "summaries", expression = "java(java.util.Collections.emptyList())")
    FileAssetDto toDto(FileAsset entity);

    List<FileAssetDto> toDtoList(List<FileAsset> entities);
}
