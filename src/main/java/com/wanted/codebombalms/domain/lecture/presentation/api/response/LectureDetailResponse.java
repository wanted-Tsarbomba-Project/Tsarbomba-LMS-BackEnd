package com.wanted.codebombalms.domain.lecture.presentation.api.response;

import com.wanted.codebombalms.domain.lecture.domain.model.Lecture;
import com.wanted.codebombalms.domain.lecture.domain.model.LectureStatus;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class LectureDetailResponse {

    private Long lectureId;
    private Long courseId;
    private Long instructorId;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    private LectureStatus status;
    private Integer lectureOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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