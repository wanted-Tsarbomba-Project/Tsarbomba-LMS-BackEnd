package com.wanted.codebombalms.problems.set.domain.model;

public class ProblemDetail {

    private final Long problemId;
    private final Integer problemNumber;
    private final String title;
    private final String content;
    private final String problemType;
    private final Integer point;
    private final String startCode;

    private ProblemDetail(
            Long problemId,
            Integer problemNumber,
            String title,
            String content,
            String problemType,
            Integer point,
            String startCode
    ) {
        this.problemId = problemId;
        this.problemNumber = problemNumber;
        this.title = title;
        this.content = content;
        this.problemType = problemType;
        this.point = point;
        this.startCode = startCode;
    }

    public static ProblemDetail of(
            Long problemId,
            Integer problemNumber,
            String title,
            String content,
            String problemType,
            Integer point
    ) {
        return new ProblemDetail(problemId, problemNumber, title, content, problemType, point, null);
    }

    public ProblemDetail withStartCode(String startCode) {
        return new ProblemDetail(problemId, problemNumber, title, content, problemType, point, startCode);
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

    public String getProblemType() {
        return problemType;
    }

    public Integer getPoint() {
        return point;
    }

    public String getStartCode() {
        return startCode;
    }
}