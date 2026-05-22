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
    private EnrollmentStatus status;
    private LocalDateTime enrolledAt;
    private LocalDateTime canceledAt;

    public static Enrollment create(Long userId, Long courseId) {
        Enrollment enrollment = new Enrollment();
        enrollment.userId = userId;
        enrollment.courseId = courseId;
        enrollment.status = EnrollmentStatus.ACTIVE;
        return enrollment;
    }

    public static Enrollment restore(
            Long enrollmentId,
            Long userId,
            Long courseId,
            EnrollmentStatus status,
            LocalDateTime enrolledAt,
            LocalDateTime canceledAt
    ) {
        return new Enrollment(enrollmentId, userId, courseId, status, enrolledAt, canceledAt);
    }

    public void cancel() {
        this.status = EnrollmentStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }
}
