package com.insightweave.dto;

import jakarta.validation.constraints.NotBlank;

public record DocumentCreateRequest(
        @NotBlank String title,
        String content
) {}
