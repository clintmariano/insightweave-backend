package com.insightweave.dto;

import java.time.Instant;
import java.util.List;

public record FileAssetDto(
        Long id,
        String originalFilename,
        String contentType,
        long sizeBytes,
        Instant createdAt,
        Instant updatedAt,
        String extractedText,
        List<SummaryDto> summaries
) {}