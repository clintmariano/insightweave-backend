package com.insightweave.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for asynchronous summary generation operations.
 * Separated into its own service to avoid self-invocation issues with @Async.
 * This service does not use @Transactional since it's just a wrapper - the actual
 * transactional work happens in SummaryService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncSummaryService {

    private final SummaryService summaryService;

    /**
     * Asynchronously generate a summary for extracted text.
     * This runs in a separate thread and won't block the file upload response.
     * Exceptions are caught and logged but don't propagate, so summary failures
     * don't affect the file upload.
     *
     * @param fileAssetId the file asset ID
     * @param extractedText the text to summarize
     */
    @Async
    public void generateSummaryAsync(Long fileAssetId, String extractedText) {
        try {
            log.info("Starting async summary generation for fileAssetId={}", fileAssetId);
            summaryService.generateSummary(fileAssetId, extractedText);
            log.info("Async summary generation completed for fileAssetId={}", fileAssetId);
        } catch (Exception e) {
            // Log error but don't fail - summary generation is optional
            log.error("Failed to generate summary for fileAssetId={}: {}", fileAssetId, e.getMessage());
        }
    }
}
