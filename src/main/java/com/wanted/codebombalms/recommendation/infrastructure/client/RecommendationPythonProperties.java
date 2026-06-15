package com.wanted.codebombalms.recommendation.infrastructure.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 추천 시스템 전용 Python FastAPI 호출 설정입니다. */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "recommendation.python")
public class RecommendationPythonProperties {

    private boolean enabled = true;
    private String generatePath = "/internal/recommendations/problem-sets/generate";
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 300000;
}
