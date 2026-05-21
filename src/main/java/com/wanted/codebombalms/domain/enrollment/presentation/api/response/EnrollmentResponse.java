package com.wanted.codebombalms.domain.enrollment.presentation.api.response;

import com.wanted.codebombalms.domain.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.domain.enrollment.domain.model.EnrollmentStatus;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class EnrollmentResponse {

    private Long enrollmentId;
    private Long courseId;
    private Long studentId;
    private EnrollmentStatus status;
    private LocalDateTime enrolledAt;
    private LocalDateTime canceledAt;

    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getEnrollmentId(),
                enrollment.getCourse().getCourseId(),
                enrollment.getStudent().getUserId(),
                enrollment.getStatus(),
                enrollment.getEnrolledAt(),
                enrollment.getCanceledAt()
        );
    }
}