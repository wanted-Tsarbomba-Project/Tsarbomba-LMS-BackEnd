package com.wanted.codebombalms.problems.result.domain.model;

import java.util.List;

public class ProblemSetResult {

    private final Long problemSetId;
    private final String title;
    private final Boolean completed;
    private final Double accuracyRate;
    private final Integer totalCompletedUserCount;
    private final Integer correctCompletedUserCount;
    private final List<ProblemSubmissionResult> submissions;

    private ProblemSetResult(
            Long problemSetId,
            String title,
            Boolean completed,
            Double accuracyRate,
            Integer totalCompletedUserCount,
            Integer correctCompletedUserCount,
            List<ProblemSubmissionResult> submissions
    ) {
        this.problemSetId = problemSetId;
        this.title = title;
        this.completed = completed;
        this.accuracyRate = accuracyRate;
        this.totalCompletedUserCount = totalCompletedUserCount;
        this.correctCompletedUserCount = correctCompletedUserCount;
        this.submissions = submissions;
    }

    public static ProblemSetResult completed(
            Long problemSetId,
            String title,
            Integer startedUserCount,
            Integer completedUserCount,
            List<ProblemSubmissionResult> submissions
    ) {
        int startedCount = toZeroIfNull(startedUserCount);
        int completedCount = toZeroIfNull(completedUserCount);

        return new ProblemSetResult(
                problemSetId,
                title,
                true,
                calculateRate(completedCount, startedCount),
                startedCount,
                completedCount,
                submissions
        );
    }

    private static Double calculateRate(int completedUserCount, int startedUserCount) {
        if (startedUserCount == 0) {
            return 0.0;
        }

        double rate = completedUserCount * 100.0 / startedUserCount;
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

    public String getTitle() {
        return title;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public Double getAccuracyRate() {
        return accuracyRate;
    }

    public Integer getTotalCompletedUserCount() {
        return totalCompletedUserCount;
    }

    public Integer getCorrectCompletedUserCount() {
        return correctCompletedUserCount;
    }

    public List<ProblemSubmissionResult> getSubmissions() {
        return submissions;
    }
}
