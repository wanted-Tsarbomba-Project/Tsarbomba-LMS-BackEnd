package com.wanted.codebombalms.problems.set.domain.model;

import java.time.LocalDateTime;

public class ProblemSetSummary {

    private final Long problemSetId;
    private final Integer problemNumber;
    private final String title;
    private final String description;
    private final String difficulty;
    private final Double accuracyRate;
    private final LocalDateTime createdAt;

    private ProblemSetSummary(
            Long problemSetId,
            Integer problemNumber,
            String title,
            String description,
            String difficulty,
            Double accuracyRate,
            LocalDateTime createdAt
    ) {
        this.problemSetId = problemSetId;
        this.problemNumber = problemNumber;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.accuracyRate = accuracyRate;
        this.createdAt = createdAt;
    }

    public static ProblemSetSummary of(
            Long problemSetId,
            Integer problemNumber,
            String title,
            String description,
            String difficulty,
            Integer completedUserCount,
            Integer startedUserCount,
            LocalDateTime createdAt
    ) {
        return new ProblemSetSummary(
                problemSetId,
                problemNumber,
                title,
                description,
                difficulty,
                calculateAccuracyRate(completedUserCount, startedUserCount),
                createdAt
        );
    }

    private static Double calculateAccuracyRate(Integer completedUserCount, Integer startedUserCount) {
        int completedCount = toZeroIfNull(completedUserCount);
        int startedCount = toZeroIfNull(startedUserCount);

        if (startedCount == 0) {
            return 0.0;
        }

        double rate = completedCount * 100.0 / startedCount;
        return Math.round(rate * 10) / 10.0;
    }

    private static int toZeroIfNull(Integer value) {
        if (value == null) {
            return 0;
        }

        return value;
    }

    public Long getProblemSetId() {
        return problemSetId;
    }

    public Integer getProblemNumber() {
        return problemNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public Double getAccuracyRate() {
        return accuracyRate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
