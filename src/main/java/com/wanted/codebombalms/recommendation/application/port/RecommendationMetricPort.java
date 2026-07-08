package com.wanted.codebombalms.recommendation.application.port;

/** Recommendation 도메인 metric 기록 port입니다. */
public interface RecommendationMetricPort {

    void recordProblemSetList(long elapsedNanos);

    void recordGenerationBatch(long elapsedNanos);

    void incrementGenerationUsers(int userCount);

    void incrementGenerationFailed(String reason);

    void incrementExposed(int exposedCount);

    void incrementHideToday();
}
