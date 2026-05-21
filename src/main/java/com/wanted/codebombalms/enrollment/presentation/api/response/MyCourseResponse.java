package com.wanted.codebombalms.enrollment.presentation.api.response;

import com.wanted.codebombalms.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MyCourseResponse {

    private Long enrollmentId;
    private Long courseId;
    private String courseTitle;
    private String courseDescription;
    private String courseThumbnailUrl;
    private EnrollmentStatus status;
    private LocalDateTime enrolledAt;

    public static MyCourseResponse from(Enrollment enrollment) {
        return new MyCourseResponse(
                enrollment.getEnrollmentId(),
                enrollment.getCourse().getCourseId(),
                enrollment.getCourse().getTitle(),
                enrollment.getCourse().getDescription(),
                enrollment.getCourse().getThumbnailUrl(),
                enrollment.getStatus(),
                enrollment.getEnrolledAt()
        );
    }
}