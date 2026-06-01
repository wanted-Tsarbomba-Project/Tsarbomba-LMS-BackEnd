package com.wanted.codebombalms.problems.category.infrastructure.persistence;

import com.wanted.codebombalms.problems.category.domain.model.ProblemCategoryStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "problem_category")

public class ProblemCategoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(nullable = false)
    private String categoryName;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemCategoryStatus status;

    protected ProblemCategoryJpaEntity() {
    }

    public ProblemCategoryJpaEntity(String categoryName, String description) {
        this.categoryName = categoryName;
        this.description = description;
        this.status = ProblemCategoryStatus.ACTIVE;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getDescription() {
        return description;
    }

    public ProblemCategoryStatus getStatus() {
        return status;
    }
}
