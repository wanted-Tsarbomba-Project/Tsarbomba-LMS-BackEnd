package com.wanted.codebombalms.domain.course.dto.response;

import com.wanted.codebombalms.domain.course.entity.Course;
import com.wanted.codebombalms.domain.course.enums.CourseStatus;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CourseDetailResponse {

    private Long courseId;
    private Long instructorId;
    private String title;
    private String description;
    private String thumbnailUrl;
    private CourseStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseDetailResponse from(Course course) {
        return new CourseDetailResponse(
                course.getCourseId(),
                course.getInstructorId(),
                course.getTitle(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getStatus(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}