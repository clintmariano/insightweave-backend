package com.insightweave.dto;

import java.time.Instant;

public record FileAssetDto(
        Long id,
        String originalFilename,
        String contentType,
        long sizeBytes,
        Instant createdAt,
        Instant updatedAt
) {}