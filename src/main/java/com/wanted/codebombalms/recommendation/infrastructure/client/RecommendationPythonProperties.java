package com.wanted.codebombalms.recommendation.infrastructure.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/** 추천 시스템 전용 Python FastAPI 호출 설정입니다. */
@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "recommendation.python")
public class RecommendationPythonProperties {

    private boolean enabled = true;

    @NotBlank
    private String generatePath = "/internal/recommendations/problem-sets/generate";

    @Positive
    private int connectTimeoutMs = 3000;

    @Positive
    private int readTimeoutMs = 300000;
}
