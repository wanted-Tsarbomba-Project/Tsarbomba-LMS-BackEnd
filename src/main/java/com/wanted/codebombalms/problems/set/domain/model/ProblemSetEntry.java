package com.wanted.codebombalms.problems.set.domain.model;

public class ProblemSetEntry {

    private final Long problemSetId;
    private final String title;
    private final String description;
    private final Integer currentProblemNumber;
    private final Boolean completed;
    private final ProblemDetail problem;

    private ProblemSetEntry(
            Long problemSetId,
            String title,
            String description,
            Integer currentProblemNumber,
            Boolean completed,
            ProblemDetail problem
    ) {
        this.problemSetId = problemSetId;
        this.title = title;
        this.description = description;
        this.currentProblemNumber = currentProblemNumber;
        this.completed = completed;
        this.problem = problem;
    }

    public static ProblemSetEntry of(
            Long problemSetId,
            String title,
            String description,
            Integer currentProblemNumber,
            Boolean completed,
            ProblemDetail problem
    ) {
        return new ProblemSetEntry(
                problemSetId,
                title,
                description,
                currentProblemNumber,
                completed,
                problem
        );
    }

    public Long getProblemSetId() {
        return problemSetId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getCurrentProblemNumber() {
        return currentProblemNumber;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public ProblemDetail getProblem() {
        return problem;
    }
}
