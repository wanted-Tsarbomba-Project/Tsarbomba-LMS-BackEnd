package com.wanted.codebombalms.problems.problem.infrastructure.persistence;

import com.wanted.codebombalms.problems.set.infrastructure.persistence.ProblemSetJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "problem")
public class ProblemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long problemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_set_id")
    private ProblemSetJpaEntity problemSet;

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

    protected ProblemJpaEntity() {
    }

    public ProblemJpaEntity(
            ProblemSetJpaEntity problemSet,
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

    public ProblemSetJpaEntity getProblemSet() {
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
