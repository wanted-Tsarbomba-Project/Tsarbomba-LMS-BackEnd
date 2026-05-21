package com.wanted.codebombalms.problems.progress.domain.model;

import com.wanted.codebombalms.problems.progress.enums.ProblemProgressStatus;

public class ProblemProgressItem {

    private final Long problemId;
    private final Integer problemNumber;
    private final ProblemProgressStatus status;

    private ProblemProgressItem(
            Long problemId,
            Integer problemNumber,
            ProblemProgressStatus status
    ) {
        this.problemId = problemId;
        this.problemNumber = problemNumber;
        this.status = status;
    }

    public static ProblemProgressItem of(
            Long problemId,
            Integer problemNumber,
            Integer currentProblemNumber,
            Boolean latestCorrect
    ) {
        return new ProblemProgressItem(
                problemId,
                problemNumber,
                decideStatus(problemNumber, currentProblemNumber, latestCorrect)
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

    public ProblemProgressStatus getStatus() {
        return status;
    }
}
