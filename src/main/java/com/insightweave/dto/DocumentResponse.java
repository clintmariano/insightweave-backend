package com.insightweave.dto;

import java.time.Instant;
import java.util.List;

public record DocumentResponse(
        Long id,
        String title,
        String content,
        Instant createdAt,
        Instant updatedAt,
        List<FileAssetDto> attachments
) {}