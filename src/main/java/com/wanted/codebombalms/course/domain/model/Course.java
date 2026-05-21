package com.wanted.codebombalms.course.domain.model;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Course {

    private Long courseId;
    private Long instructorId;
    private String title;
    private String description;
    private String thumbnailUrl;
    private CourseStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static Course create(
            Long instructorId,
            String title,
            String description,
            String thumbnailUrl
    ) {
        Course course = new Course();
        course.instructorId = instructorId;
        course.title = title;
        course.description = description;
        course.thumbnailUrl = thumbnailUrl;
        course.status = CourseStatus.DRAFT;
        return course;
    }

    public static Course restore(
            Long courseId,
            Long instructorId,
            String title,
            String description,
            String thumbnailUrl,
            CourseStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        return new Course(
                courseId,
                instructorId,
                title,
                description,
                thumbnailUrl,
                status,
                createdAt,
                updatedAt,
                deletedAt
        );
    }

    public void update(
            String title,
            String description,
            String thumbnailUrl,
            CourseStatus status
    ) {
        if (title != null) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        if (thumbnailUrl != null) {
            this.thumbnailUrl = thumbnailUrl;
        }
        if (status != null) {
            this.status = status;
        }
    }

    public void delete() {
        this.status = CourseStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public void publish() {
        this.status = CourseStatus.ACTIVE;
    }
}
