package com.wanted.codebombalms.lecture.presentation.api.response;

import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureStatus;

import java.time.LocalDateTime;

public record LectureDetailResponse(
        Long lectureId,
        Long courseId,
        Long instructorId,
        String title,
        String description,
        String videoUrl,
        String thumbnailUrl,
        LectureStatus status,
        Integer lectureOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static LectureDetailResponse from(Lecture lecture) {
        return new LectureDetailResponse(
                lecture.getLectureId(),
                lecture.getCourse().getCourseId(),
                lecture.getCourse().getInstructorId(),
                lecture.getTitle(),
                lecture.getDescription(),
                lecture.getVideoUrl(),
                lecture.getThumbnailUrl(),
                lecture.getStatus(),
                lecture.getLectureOrder(),
                lecture.getCreatedAt(),
                lecture.getUpdatedAt()
        );
    }
}
