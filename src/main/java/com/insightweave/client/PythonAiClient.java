package com.insightweave.client;

import com.insightweave.client.dto.SummarizeRequest;
import com.insightweave.client.dto.SummarizeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class PythonAiClient {

    private final RestTemplate restTemplate;

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    @Value("${ai.service.timeout:30000}")
    private long timeout;

    /**
     * Call the Python AI service to generate a summary.
     *
     * @param text the text to summarize
     * @param maxLength maximum length of summary in tokens
     * @param minLength minimum length of summary in tokens
     * @param style summary style (concise, detailed, bullet_points)
     * @return the summarization response
     * @throws RestClientException if the HTTP call fails
     */
    public SummarizeResponse summarize(String text, Integer maxLength, Integer minLength, String style) {
        log.info("Calling Python AI service to summarize {} characters", text.length());

        var request = new SummarizeRequest(
            text,
            maxLength != null ? maxLength : 150,
            minLength != null ? minLength : 50,
            style != null ? style : "concise"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SummarizeRequest> entity = new HttpEntity<>(request, headers);

        try {
            String url = aiServiceUrl + "/nlp/summarize";
            log.info("POST {} with request: text length={}, maxLength={}, minLength={}, style={}",
                url, request.text().length(), request.maxLength(), request.minLength(), request.style());

            ResponseEntity<SummarizeResponse> response = restTemplate.postForEntity(
                url,
                entity,
                SummarizeResponse.class
            );

            if (response.getBody() == null) {
                throw new RestClientException("Empty response from AI service");
            }

            log.info("Summary generated successfully in {}ms", response.getBody().latencyMs());
            return response.getBody();

        } catch (RestClientException e) {
            log.error("Failed to call Python AI service: {}", e.getMessage());
            throw new RestClientException("AI service unavailable: " + e.getMessage(), e);
        }
    }

    /**
     * Check if the Python AI service is healthy.
     *
     * @return true if service is healthy, false otherwise
     */
    public boolean isHealthy() {
        try {
            String url = aiServiceUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("AI service health check failed: {}", e.getMessage());
            return false;
        }
    }
}
