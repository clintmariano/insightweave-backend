package com.insightweave.service;

import com.insightweave.client.PythonAiClient;
import com.insightweave.client.dto.SummarizeResponse;
import com.insightweave.dto.SummaryDto;
import com.insightweave.entity.Summary;
import com.insightweave.mapper.SummaryMapper;
import com.insightweave.repository.SummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryService {

    private final PythonAiClient aiClient;
    private final SummaryRepository summaryRepo;
    private final SummaryMapper summaryMapper;

    /**
     * Generate a summary for the given text and save it to the database.
     *
     * @param fileAssetId the ID of the file asset being summarized
     * @param extractedText the text to summarize
     * @return the generated summary DTO
     * @throws IllegalArgumentException if text is null or empty
     * @throws RestClientException if the AI service call fails
     */
    @Transactional
    public SummaryDto generateSummary(Long fileAssetId, String extractedText) {
        return generateSummary(fileAssetId, extractedText, null, null, null);
    }

    /**
     * Generate a summary with custom parameters.
     *
     * @param fileAssetId the ID of the file asset being summarized
     * @param extractedText the text to summarize
     * @param maxLength maximum summary length in tokens (default: 150)
     * @param minLength minimum summary length in tokens (default: 50)
     * @param style summary style: concise, detailed, or bullet_points (default: concise)
     * @return the generated summary DTO
     */
    @Transactional
    public SummaryDto generateSummary(Long fileAssetId, String extractedText,
                                     Integer maxLength, Integer minLength, String style) {

        if (extractedText == null || extractedText.isBlank()) {
            throw new IllegalArgumentException("Cannot generate summary: extracted text is empty");
        }

        log.info("Generating summary for fileAssetId={}, textLength={}", fileAssetId, extractedText.length());

        try {
            // Call Python AI service
            SummarizeResponse response = aiClient.summarize(
                extractedText,
                maxLength,
                minLength,
                style
            );

            // Save to database
            Summary summary = Summary.builder()
                .fileAssetId(fileAssetId)
                .summaryText(response.summary())
                .modelName(response.modelName())
                .style(response.style())
                .latencyMs(response.latencyMs())
                .build();

            summary = summaryRepo.save(summary);
            log.info("Summary saved with id={}", summary.getId());

            return summaryMapper.toDto(summary);

        } catch (RestClientException e) {
            log.error("Failed to generate summary for fileAssetId={}: {}", fileAssetId, e.getMessage());
            throw e;
        }
    }

    /**
     * Get all summaries for a file asset.
     *
     * @param fileAssetId the file asset ID
     * @return list of summaries (may be empty)
     */
    @Transactional(readOnly = true)
    public List<SummaryDto> getSummariesForFile(Long fileAssetId) {
        List<Summary> summaries = summaryRepo.findByFileAssetId(fileAssetId);
        return summaryMapper.toDtoList(summaries);
    }

    /**
     * Get the most recent summary for a file asset.
     *
     * @param fileAssetId the file asset ID
     * @return the latest summary, or null if none exist
     */
    @Transactional(readOnly = true)
    public SummaryDto getLatestSummary(Long fileAssetId) {
        return summaryRepo.findFirstByFileAssetIdOrderByCreatedAtDesc(fileAssetId)
            .map(summaryMapper::toDto)
            .orElse(null);
    }

    /**
     * Delete all summaries for a file asset.
     *
     * @param fileAssetId the file asset ID
     */
    @Transactional
    public void deleteSummariesForFile(Long fileAssetId) {
        log.info("Deleting all summaries for fileAssetId={}", fileAssetId);
        summaryRepo.deleteByFileAssetId(fileAssetId);
    }
}
