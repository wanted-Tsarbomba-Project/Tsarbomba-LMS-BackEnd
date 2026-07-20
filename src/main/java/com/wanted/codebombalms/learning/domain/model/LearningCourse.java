package com.wanted.codebombalms.learning.domain.model;

public record LearningCourse(
        Long courseId,
        String title,
        String description
) {

    public LearningCourse(Long courseId, String title) {
        this(courseId, title, null);
    }
}
