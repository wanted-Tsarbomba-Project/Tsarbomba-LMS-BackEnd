package com.wanted.codebombalms.learning.infrastructure.client;

import com.wanted.codebombalms.global.domain.common.error.exception.ExternalServiceException;
import com.wanted.codebombalms.learning.application.port.LearningRecommendationClient;
import com.wanted.codebombalms.learning.domain.exception.LearningErrorCode;
import io.netty.channel.ChannelOption;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/** 강좌 완료 추천 전용 FastAPI client입니다. */
@Slf4j
@Component
public class FastApiLearningRecommendationClient implements LearningRecommendationClient {

    private static final int MAX_RECOMMENDATION_COUNT = 2;
    private static final int MAX_REASON_LENGTH = 300;
    private static final int MAX_RESPONSE_IN_MEMORY_BYTES = 512 * 1024;

    private final LearningRecommendationProperties properties;
    private final WebClient webClient;

    public FastApiLearningRecommendationClient(
            LearningRecommendationProperties properties,
            @Value("${fastapi.url}") String fastApiBaseUrl
    ) {
        this.properties = properties;
        this.webClient = buildWebClient(fastApiBaseUrl, properties);
    }

    @Override
    public LearningRecommendationResult rankFinalProblemSets(LearningRecommendationRequest request) {
        if (!properties.isEnabled()) {
            throw new ExternalServiceException(LearningErrorCode.LEARNING_RECOMMENDATION_UNAVAILABLE);
        }

        long startedAt = System.nanoTime();
        try {
            LearningRecommendationResult response = webClient.post()
                    .uri(properties.getRankPath())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(LearningRecommendationResult.class)
                    .block();

            validateResponse(response);
            log.info(
                    "event=learning_recommendation_called recommendationCount={} durationMs={}",
                    response.recommendations().size(),
                    (System.nanoTime() - startedAt) / 1_000_000
            );
            return response;
        } catch (ExternalServiceException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.warn(
                    "event=learning_recommendation_failed exceptionType={} durationMs={}",
                    exception.getClass().getSimpleName(),
                    (System.nanoTime() - startedAt) / 1_000_000
            );
            throw new ExternalServiceException(
                    LearningErrorCode.LEARNING_RECOMMENDATION_UNAVAILABLE,
                    exception
            );
        }
    }

    private void validateResponse(LearningRecommendationResult response) {
        if (response == null
                || response.algorithm() == null
                || response.algorithm().isBlank()
                || response.recommendations() == null
                || response.recommendations().size() > MAX_RECOMMENDATION_COUNT) {
            throw new ExternalServiceException(LearningErrorCode.LEARNING_RECOMMENDATION_INVALID_RESPONSE);
        }

        Set<Long> ids = new HashSet<>();
        for (RankedProblemSet item : response.recommendations()) {
            if (!isValidItem(item) || !ids.add(item.problemSetId())) {
                throw new ExternalServiceException(LearningErrorCode.LEARNING_RECOMMENDATION_INVALID_RESPONSE);
            }
        }
    }

    private boolean isValidItem(RankedProblemSet item) {
        if (item == null
                || item.problemSetId() == null
                || item.problemSetId() <= 0
                || item.score() == null
                || item.reasonCode() == null
                || item.recommendationReason() == null
                || item.recommendationReason().isBlank()
                || item.recommendationReason().length() > MAX_REASON_LENGTH) {
            return false;
        }
        return item.score().compareTo(BigDecimal.ZERO) >= 0
                && item.score().compareTo(BigDecimal.ONE) <= 0;
    }

    private WebClient buildWebClient(
            String fastApiBaseUrl,
            LearningRecommendationProperties properties
    ) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnectTimeoutMs())
                .responseTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));

        return WebClient.builder()
                .baseUrl(fastApiBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize(MAX_RESPONSE_IN_MEMORY_BYTES))
                .build();
    }
}
