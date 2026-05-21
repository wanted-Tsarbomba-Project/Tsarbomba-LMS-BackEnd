package com.wanted.codebombalms.lecture.application.command;

import com.wanted.codebombalms.lecture.domain.model.LectureStatus;

public record UpdateLectureCommand(
        Long lectureId,
        String title,
        String description,
        String videoUrl,
        String thumbnailUrl,
        Integer lectureOrder,
        LectureStatus status
) {
}
