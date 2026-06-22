package com.wanted.codebombalms.lecture.application.command;

import com.wanted.codebombalms.lecture.domain.model.LectureStatus;

public record CreateLectureCommand(
        Long courseId,
        String title,
        String description,
        String videoUrl,
        String thumbnailUrl,
        Long problemCategoryId,
        Integer lectureOrder,
        LectureStatus status
) {
}
