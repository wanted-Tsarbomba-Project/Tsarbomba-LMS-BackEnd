package com.wanted.codebombalms.problems.set.domain.model;

// 챗봇 adapter용 최소 조회 모델 (problemSetId, title, description 만)
public class ProblemSetBrief {

    private final Long problemSetId;
    private final String title;
    private final String description;

    private ProblemSetBrief(Long problemSetId, String title, String description) {
        this.problemSetId = problemSetId;
        this.title = title;
        this.description = description;
    }

    public static ProblemSetBrief of(Long problemSetId, String title, String description) {
        return new ProblemSetBrief(problemSetId, title, description);
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
}
