package com.wanted.codebombalms.problems.problem.domain.model;

public class Problem {

    private final Long problemId;
    private final Long problemSetId;
    private final Integer problemOrder;
    private final String title;
    private final String content;
    private final String problemType;
    private final String explanation;
    private final Integer point;
    private final Integer attemptLimit;
    private final Boolean retriable;

    private Problem(
            Long problemId,
            Long problemSetId,
            Integer problemOrder,
            String title,
            String content,
            String problemType,
            String explanation,
            Integer point,
            Integer attemptLimit,
            Boolean retriable
    ) {
        this.problemId = problemId;
        this.problemSetId = problemSetId;
        this.problemOrder = problemOrder;
        this.title = title;
        this.content = content;
        this.problemType = problemType;
        this.explanation = explanation;
        this.point = point;
        this.attemptLimit = attemptLimit;
        this.retriable = retriable;
    }

    public static Problem restore(
            Long problemId,
            Long problemSetId,
            Integer problemOrder,
            String title,
            String content,
            String problemType,
            String explanation,
            Integer point,
            Integer attemptLimit,
            Boolean retriable
    ) {
        return new Problem(
                problemId,
                problemSetId,
                problemOrder,
                title,
                content,
                problemType,
                explanation,
                point,
                attemptLimit,
                retriable
        );
    }

    public Long getProblemId() {
        return problemId;
    }

    public Long getProblemSetId() {
        return problemSetId;
    }

    public Integer getProblemOrder() {
        return problemOrder;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getProblemType() {
        return problemType;
    }

    public String getExplanation() {
        return explanation;
    }

    public Integer getPoint() {
        return point;
    }

    public Integer getAttemptLimit() {
        return attemptLimit;
    }

    public Boolean getRetriable() {
        return retriable;
    }
}
