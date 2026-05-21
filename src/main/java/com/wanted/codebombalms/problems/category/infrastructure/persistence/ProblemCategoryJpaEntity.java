package com.wanted.codebombalms.problems.category.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "problem_category")
public class ProblemCategoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(nullable = false)
    private String categoryName;

    private String description;

    @Column(nullable = false)
    private String status;

    protected ProblemCategoryJpaEntity() {
    }

    public ProblemCategoryJpaEntity(String categoryName, String description) {
        this.categoryName = categoryName;
        this.description = description;
        this.status = "ACTIVE";
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

    public String getStatus() {
        return status;
    }
}
