package com.wanted.codebombalms.learning.application.port;

public record LearningLecture(
        Long lectureId,
        String title,
        String description
) {

    public LearningLecture(Long lectureId, String title) {
        this(lectureId, title, null);
    }
}
