package com.wanted.codebombalms.domain.enrollment.domain.model;

import com.wanted.codebombalms.domain.course.domain.model.Course;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Enrollment {

    private Long enrollmentId;
    private Course course;
    private Long studentId;
    private EnrollmentStatus status;
    private LocalDateTime enrolledAt;
    private LocalDateTime canceledAt;

    public static Enrollment create(Course course, Long studentId) {
        Enrollment enrollment = new Enrollment();
        enrollment.course = course;
        enrollment.studentId = studentId;
        enrollment.status = EnrollmentStatus.ACTIVE;
        return enrollment;
    }

    public static Enrollment restore(
            Long enrollmentId,
            Course course,
            Long studentId,
            EnrollmentStatus status,
            LocalDateTime enrolledAt,
            LocalDateTime canceledAt
    ) {
        return new Enrollment(enrollmentId, course, studentId, status, enrolledAt, canceledAt);
    }

    public void cancel() {
        this.status = EnrollmentStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }
}
