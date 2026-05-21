package com.wanted.codebombalms.domain.enrollment.domain.model;

import com.wanted.codebombalms.domain.course.domain.model.Course;
import com.wanted.codebombalms.domain.user.entity.User;
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
    private User student;
    private EnrollmentStatus status;
    private LocalDateTime enrolledAt;
    private LocalDateTime canceledAt;

    public static Enrollment create(Course course, User student) {
        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        return enrollment;
    }

    public static Enrollment restore(
            Long enrollmentId,
            Course course,
            User student,
            EnrollmentStatus status,
            LocalDateTime enrolledAt,
            LocalDateTime canceledAt
    ) {
        return new Enrollment(
                enrollmentId,
                course,
                student,
                status,
                enrolledAt,
                canceledAt
        );
    }

    public void cancel() {
        this.status = EnrollmentStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }
}
