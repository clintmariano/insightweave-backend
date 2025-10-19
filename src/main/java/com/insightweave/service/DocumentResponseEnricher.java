package com.insightweave.service;

import com.insightweave.dto.DocumentResponse;
import com.insightweave.dto.FileAssetDto;
import com.insightweave.mapper.SummaryMapper;
import com.insightweave.repository.SummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DocumentResponseEnricher {

    private final SummaryRepository summaryRepository;
    private final SummaryMapper summaryMapper;

    /**
     * Enrich a DocumentResponse by loading summaries for all attachments.
     *
     * @param response the document response to enrich
     * @return the enriched document response with summaries populated
     */
    public DocumentResponse enrich(DocumentResponse response) {
        if (response == null || response.attachments() == null) {
            return response;
        }

        List<FileAssetDto> enrichedAttachments = response.attachments().stream()
            .map(this::enrichFileAsset)
            .collect(Collectors.toList());

        return new DocumentResponse(
            response.id(),
            response.title(),
            response.content(),
            response.createdAt(),
            response.updatedAt(),
            enrichedAttachments
        );
    }

    private FileAssetDto enrichFileAsset(FileAssetDto fileAsset) {
        var summaries = summaryRepository.findByFileAssetId(fileAsset.id());
        var summaryDtos = summaryMapper.toDtoList(summaries);

        return new FileAssetDto(
            fileAsset.id(),
            fileAsset.originalFilename(),
            fileAsset.contentType(),
            fileAsset.sizeBytes(),
            fileAsset.createdAt(),
            fileAsset.updatedAt(),
            fileAsset.extractedText(),
            summaryDtos
        );
    }
}
