package com.wanted.codebombalms.problems.recommendation.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "problem_recommended_course")
public class ProblemRecommendedCourseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_recommended_course_id")
    private Long problemRecommendedCourseId;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private static final String ACTIVE_STATUS = "ACTIVE";

    protected ProblemRecommendedCourseJpaEntity() {
    }

    public ProblemRecommendedCourseJpaEntity(
            Long problemId,
            Long courseId,
            Integer displayOrder
    ) {
        this.problemId = problemId;
        this.courseId = courseId;
        this.displayOrder = displayOrder;
        this.status = ACTIVE_STATUS;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.status == null) {
            this.status = ACTIVE_STATUS;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getCourseId() {
        return courseId;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}
