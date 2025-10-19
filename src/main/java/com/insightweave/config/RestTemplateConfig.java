package com.insightweave.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Configuration
public class RestTemplateConfig {

    @Value("${ai.service.timeout:30000}")
    private long aiServiceTimeout;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Configure Apache HttpClient with proper timeouts
        var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setDefaultSocketConfig(org.apache.hc.core5.http.io.SocketConfig.custom()
                .setSoTimeout(Timeout.ofMilliseconds(aiServiceTimeout))
                .build())
            .build();

        HttpClient httpClient = HttpClientBuilder.create()
            .setConnectionManager(connectionManager)
            .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectTimeout(Duration.ofMillis(5000));

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getInterceptors().add(new LoggingInterceptor());

        // Ensure UTF-8 encoding for all string conversions
        restTemplate.getMessageConverters()
            .stream()
            .filter(converter -> converter instanceof StringHttpMessageConverter)
            .findFirst()
            .ifPresent(converter -> ((StringHttpMessageConverter) converter).setDefaultCharset(StandardCharsets.UTF_8));

        return restTemplate;
    }

    static class LoggingInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            // Log at DEBUG level to avoid cluttering production logs
            // Can be enabled by setting logging.level.com.insightweave.config=DEBUG
            if (log.isDebugEnabled()) {
                log.debug("RestTemplate Request: {} {} - Body length: {} bytes",
                    request.getMethod(),
                    request.getURI(),
                    body.length);
            }
            return execution.execute(request, body);
        }
    }
}
