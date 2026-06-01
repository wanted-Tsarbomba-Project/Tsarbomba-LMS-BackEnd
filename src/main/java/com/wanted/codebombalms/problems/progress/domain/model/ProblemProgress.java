package com.wanted.codebombalms.problems.progress.domain.model;

import com.wanted.codebombalms.problems.progress.enums.ProblemProgressStatus;

import java.util.List;

public class ProblemProgress {

    private final Long problemSetId;
    private final Integer currentProblemNumber;
    private final List<ProblemProgressItem> problems;

    private ProblemProgress(
            Long problemSetId,
            Integer currentProblemNumber,
            List<ProblemProgressItem> problems
    ) {
        this.problemSetId = problemSetId;
        this.currentProblemNumber = currentProblemNumber;
        this.problems = problems;
    }

    public static ProblemProgress of(
            Long problemSetId,
            Integer currentProblemNumber,
            List<ProblemProgressItem> problems
    ) {
        return new ProblemProgress(problemSetId, currentProblemNumber, problems);
    }

    public Long getProblemSetId() {
        return problemSetId;
    }

    public Integer getTotalProblemCount() {
        return problems.size();
    }

    public Integer getCurrentProblemNumber() {
        return currentProblemNumber;
    }

    public Long getCurrentProblemId() {
        return problems.stream()
                .filter(problem -> problem.getProblemNumber().equals(currentProblemNumber))
                .map(ProblemProgressItem::getProblemId)
                .findFirst()
                .orElse(null);
    }

    public Integer getSolvedProblemCount() {
        return (int) problems.stream()
                .filter(problem -> problem.getStatus() == ProblemProgressStatus.CORRECT)
                .count();
    }

    public List<ProblemProgressItem> getProblems() {
        return problems;
    }
}
