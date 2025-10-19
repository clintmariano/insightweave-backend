package com.insightweave.dto;

import java.time.Instant;

public record SummaryDto(
        Long id,
        String summaryText,
        String modelName,
        String style,
        Integer latencyMs,
        Instant createdAt
) {}
