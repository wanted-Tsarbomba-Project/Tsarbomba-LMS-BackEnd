package com.wanted.codebombalms.domain.problems.problem.entitiy;

import com.wanted.codebombalms.domain.problems.set.entity.ProblemSet;
import jakarta.persistence.*;

@Entity
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long problemId;

    @ManyToOne
    @JoinColumn(name = "problem_set_id")
    private ProblemSet problemSet;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String problemType;

    private String difficulty;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(nullable = false)
    private int score;

    private Integer attemptLimit;

    private Boolean isRetriable;

    @Column(nullable = false)
    private String status;

    private Integer problemOrder;

    protected Problem() {
    }
    public Problem(
            ProblemSet problemSet,
            String title,
            String content,
            String answer,
            String explanation,
            int score,
            Integer problemOrder
    ) {
        this.problemSet = problemSet;
        this.title = title;
        this.content = content;
        this.problemType = "TEXT";
        this.difficulty = "EASY";
        this.answer = answer;
        this.explanation = explanation;
        this.score = score;
        this.attemptLimit = 3;
        this.isRetriable = true;
        this.status = "ACTIVE";
        this.problemOrder = problemOrder;
    }

    public void update(
            String title,
            String content,
            String answer,
            String explanation,
            int score
    ) {
        this.title = title;
        this.content = content;
        this.answer = answer;
        this.explanation = explanation;
        this.score = score;
    }

    public void deactivate() {
        this.status = "INACTIVE";
    }

    public Long getProblemId() {
        return problemId;
    }

    public ProblemSet getProblemSet() {
        return problemSet;
    }

    public String getTitle() {
        return title;
    }

    public Integer getProblemOrder() {
        return problemOrder;
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

    public int getScore() {
        return score;
    }

    public Integer getAttemptLimit() {
        return attemptLimit;
    }

    public Boolean getRetriable() {
        return isRetriable;
    }
}
