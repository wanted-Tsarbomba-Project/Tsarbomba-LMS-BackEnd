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
    private Long courseCategoryId;
    private String courseCategoryName;
    private String title;
    private String description;
    private String thumbnailUrl;
    private CourseStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static Course create(
            Long instructorId,
            Long courseCategoryId,
            String title,
            String description,
            String thumbnailUrl
    ) {
        Course course = new Course();
        course.instructorId = instructorId;
        course.courseCategoryId = courseCategoryId;
        course.title = title;
        course.description = description;
        course.thumbnailUrl = thumbnailUrl;
        course.status = CourseStatus.DRAFT;
        return course;
    }

    public static Course restore(
            Long courseId,
            Long instructorId,
            Long courseCategoryId,
            String courseCategoryName,
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
                courseCategoryId,
                courseCategoryName,
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
            Long courseCategoryId,
            String title,
            String description,
            String thumbnailUrl,
            CourseStatus status
    ) {
        if (courseCategoryId != null) {
            this.courseCategoryId = courseCategoryId;
        }
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
