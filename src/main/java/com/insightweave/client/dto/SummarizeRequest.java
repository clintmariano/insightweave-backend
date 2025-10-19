package com.insightweave.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SummarizeRequest(
    String text,
    @JsonProperty("max_length") Integer maxLength,
    @JsonProperty("min_length") Integer minLength,
    String style
) {}
