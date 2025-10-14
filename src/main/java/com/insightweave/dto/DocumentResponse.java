package com.insightweave.dto;

import java.time.Instant;

public record DocumentResponse(
        Long id,
        String title,
        String content,
        Instant createdAt,
        Instant updatedAt
) {}