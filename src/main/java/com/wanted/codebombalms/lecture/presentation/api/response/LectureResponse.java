package com.wanted.codebombalms.lecture.presentation.api.response;

import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureStatus;

public record LectureResponse(
        Long lectureId,
        Long courseId,
        Long instructorId,
        String title,
        String thumbnailUrl,
        LectureStatus status,
        Integer lectureOrder
) {

    public static LectureResponse from(Lecture lecture) {
        return new LectureResponse(
                lecture.getLectureId(),
                lecture.getCourse().getCourseId(),
                lecture.getCourse().getInstructorId(),
                lecture.getTitle(),
                lecture.getThumbnailUrl(),
                lecture.getStatus(),
                lecture.getLectureOrder()
        );
    }
}
