package com.wanted.codebombalms.domain.lecture.dto.response;

import com.wanted.codebombalms.domain.lecture.entity.Lecture;
import com.wanted.codebombalms.domain.lecture.enums.LectureStatus;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class LectureResponse {

    private Long lectureId;
    private Long courseId;
    private Long instructorId;
    private String title;
    private String thumbnailUrl;
    private LectureStatus status;
    private Integer lectureOrder;

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