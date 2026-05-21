package com.wanted.codebombalms.problems.result.domain.model;

import java.time.LocalDateTime;

public class ProblemSubmissionResult {

    private final Long problemId;
    private final Integer problemNumber;
    private final String title;
    private final String content;
    private final String submittedAnswer;
    private final Boolean correct;
    private final LocalDateTime submittedAt;
    private final String explanation;

    private ProblemSubmissionResult(
            Long problemId,
            Integer problemNumber,
            String title,
            String content,
            String submittedAnswer,
            Boolean correct,
            LocalDateTime submittedAt,
            String explanation
    ) {
        this.problemId = problemId;
        this.problemNumber = problemNumber;
        this.title = title;
        this.content = content;
        this.submittedAnswer = submittedAnswer;
        this.correct = correct;
        this.submittedAt = submittedAt;
        this.explanation = explanation;
    }

    public static ProblemSubmissionResult of(
            Long problemId,
            Integer problemNumber,
            String title,
            String content,
            String submittedAnswer,
            Boolean correct,
            LocalDateTime submittedAt,
            String explanation
    ) {
        return new ProblemSubmissionResult(
                problemId,
                problemNumber,
                title,
                content,
                submittedAnswer,
                correct,
                submittedAt,
                explanation
        );
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

    public String getContent() {
        return content;
    }

    public String getSubmittedAnswer() {
        return submittedAnswer;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public String getExplanation() {
        return explanation;
    }
}
