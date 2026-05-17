package com.wanted.codebombalms.domain.course.dto.response;

import com.wanted.codebombalms.domain.course.entity.Course;
import com.wanted.codebombalms.domain.course.enums.CourseStatus;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CourseResponse {

    private Long courseId;
    private Long instructorId;
    private String title;
    private String thumbnailUrl;
    private CourseStatus status;

    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getCourseId(),
                course.getInstructorId(),
                course.getTitle(),
                course.getThumbnailUrl(),
                course.getStatus()
        );
    }
}