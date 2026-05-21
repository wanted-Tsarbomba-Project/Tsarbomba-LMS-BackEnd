package com.wanted.codebombalms.problems.problem.domain.model;

public class Problem {

    private final Long problemId;
    private final Long problemSetId;
    private final Integer problemOrder;
    private final String title;
    private final String content;
    private final String problemType;
    private final String answer;
    private final String explanation;
    private final Integer score;
    private final Integer attemptLimit;
    private final Boolean retriable;

    private Problem(
            Long problemId,
            Long problemSetId,
            Integer problemOrder,
            String title,
            String content,
            String problemType,
            String answer,
            String explanation,
            Integer score,
            Integer attemptLimit,
            Boolean retriable
    ) {
        this.problemId = problemId;
        this.problemSetId = problemSetId;
        this.problemOrder = problemOrder;
        this.title = title;
        this.content = content;
        this.problemType = problemType;
        this.answer = answer;
        this.explanation = explanation;
        this.score = score;
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
            String answer,
            String explanation,
            Integer score,
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
                answer,
                explanation,
                score,
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

    public String getAnswer() {
        return answer;
    }

    public String getExplanation() {
        return explanation;
    }

    public Integer getScore() {
        return score;
    }

    public Integer getAttemptLimit() {
        return attemptLimit;
    }

    public Boolean getRetriable() {
        return retriable;
    }
}
