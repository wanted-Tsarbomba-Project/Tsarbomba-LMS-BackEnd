package com.wanted.codebombalms.learning.infrastructure.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/** learning 도메인의 Python 추천 API 호출 설정입니다. */
@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "learning.recommendation.python")
public class LearningRecommendationProperties {

    private boolean enabled = true;

    @NotBlank
    private String rankPath = "/internal/learning/final-problem-sets/rank";

    @Positive
    private int connectTimeoutMs = 3000;

    @Positive
    private int readTimeoutMs = 10000;
}
