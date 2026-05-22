package com.wanted.codebombalms.enrollment.domain.model;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Enrollment {

    private Long enrollmentId;
    private Long userId;
    private Long courseId;
    private Long instructorId;
    private EnrollmentStatus status;
    private LocalDateTime enrolledAt;
    private LocalDateTime canceledAt;

    public static Enrollment create(Long userId, Long courseId, Long instructorId) {
        Enrollment enrollment = new Enrollment();
        enrollment.userId = userId;
        enrollment.courseId = courseId;
        enrollment.instructorId = instructorId;
        enrollment.status = EnrollmentStatus.ACTIVE;
        return enrollment;
    }

    public static Enrollment restore(
            Long enrollmentId,
            Long userId,
            Long courseId,
            Long instructorId,
            EnrollmentStatus status,
            LocalDateTime enrolledAt,
            LocalDateTime canceledAt
    ) {
        return new Enrollment(enrollmentId, userId, courseId, instructorId, status, enrolledAt, canceledAt);
    }

    public void cancel() {
        this.status = EnrollmentStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }
}
