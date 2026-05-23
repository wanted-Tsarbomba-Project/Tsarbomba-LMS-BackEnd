package com.wanted.codebombalms.course.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.course.domain.model.CourseStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@ToString
@Table(name = "course")
public class CourseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "instructor_id", nullable = false)
    private Long instructorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_category_id")
    @ToString.Exclude
    private CourseCategoryJpaEntity courseCategory;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CourseStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public CourseJpaEntity(
            Long instructorId,
            CourseCategoryJpaEntity courseCategory,
            String title,
            String description,
            String thumbnailUrl,
            CourseStatus status
    ) {
        this.instructorId = instructorId;
        this.courseCategory = courseCategory;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.status = status;
    }

    public static CourseJpaEntity from(Course course, CourseCategoryJpaEntity courseCategory) {
        CourseJpaEntity entity = new CourseJpaEntity(
                course.getInstructorId(),
                courseCategory,
                course.getTitle(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getStatus()
        );
        entity.courseId = course.getCourseId();
        entity.createdAt = course.getCreatedAt();
        entity.updatedAt = course.getUpdatedAt();
        entity.deletedAt = course.getDeletedAt();
        return entity;
    }

    public void apply(Course course, CourseCategoryJpaEntity courseCategory) {
        this.instructorId = course.getInstructorId();
        this.courseCategory = courseCategory;
        this.title = course.getTitle();
        this.description = course.getDescription();
        this.thumbnailUrl = course.getThumbnailUrl();
        this.status = course.getStatus();
        this.deletedAt = course.getDeletedAt();
    }

    public Course toDomain() {
        return Course.restore(
                courseId,
                instructorId,
                courseCategory != null ? courseCategory.getCourseCategoryId() : null,
                courseCategory != null ? courseCategory.getName() : null,
                title,
                description,
                thumbnailUrl,
                status,
                createdAt,
                updatedAt,
                deletedAt
        );
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = CourseStatus.DRAFT;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
