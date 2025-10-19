package com.insightweave.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SummarizeResponse(
    String summary,
    @JsonProperty("model_name") String modelName,
    @JsonProperty("latency_ms") Integer latencyMs,
    @JsonProperty("input_length") Integer inputLength,
    @JsonProperty("summary_length") Integer summaryLength,
    String style
) {}
