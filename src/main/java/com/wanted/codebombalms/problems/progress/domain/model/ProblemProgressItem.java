package com.wanted.codebombalms.problems.progress.domain.model;

import com.wanted.codebombalms.problems.progress.enums.ProblemProgressStatus;

public class ProblemProgressItem {

    private final Long problemId;
    private final Integer problemNumber;
    private final String title;
    private final ProblemProgressStatus status;
    private final Long latestSubmissionId;

    private ProblemProgressItem(
            Long problemId,
            Integer problemNumber,
            String title,
            ProblemProgressStatus status,
            Long latestSubmissionId
    ) {
        this.problemId = problemId;
        this.problemNumber = problemNumber;
        this.title = title;
        this.status = status;
        this.latestSubmissionId = latestSubmissionId;
    }

    public static ProblemProgressItem of(
            Long problemId,
            Integer problemNumber,
            String title,
            Integer currentProblemNumber,
            Boolean latestCorrect,
            Long latestSubmissionId
    ) {
        return new ProblemProgressItem(
                problemId,
                problemNumber,
                title,
                decideStatus(problemNumber, currentProblemNumber, latestCorrect),
                latestSubmissionId
        );
    }

    private static ProblemProgressStatus decideStatus(
            Integer problemNumber,
            Integer currentProblemNumber,
            Boolean latestCorrect
    ) {
        if (problemNumber > currentProblemNumber) {
            return ProblemProgressStatus.LOCKED;
        }

        if (latestCorrect == null) {
            return ProblemProgressStatus.UNSOLVED;
        }

        return Boolean.TRUE.equals(latestCorrect)
                ? ProblemProgressStatus.CORRECT
                : ProblemProgressStatus.WRONG;
    }

    public Long getProblemId() {
        return problemId;
    }

    public Integer getProblemNumber() {
        return problemNumber;
    }

    public String getTitle() {
        return title;
    }

    public ProblemProgressStatus getStatus() {
        return status;
    }

    public Long getLatestSubmissionId() {
        return latestSubmissionId;
    }
}
