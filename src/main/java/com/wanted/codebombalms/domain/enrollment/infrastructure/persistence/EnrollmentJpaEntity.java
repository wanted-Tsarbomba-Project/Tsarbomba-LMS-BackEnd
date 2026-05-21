package com.wanted.codebombalms.domain.enrollment.infrastructure.persistence;

import com.wanted.codebombalms.domain.course.infrastructure.persistence.CourseJpaEntity;
import com.wanted.codebombalms.domain.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.domain.enrollment.domain.model.EnrollmentStatus;
import com.wanted.codebombalms.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@ToString
@Table(name = "enrollment")
public class EnrollmentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @ToString.Exclude
    private CourseJpaEntity course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EnrollmentStatus status;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDateTime enrolledAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    public EnrollmentJpaEntity(CourseJpaEntity course, User student, EnrollmentStatus status) {
        this.course = course;
        this.student = student;
        this.status = status;
    }

    public static EnrollmentJpaEntity from(Enrollment enrollment, CourseJpaEntity course) {
        EnrollmentJpaEntity entity = new EnrollmentJpaEntity(
                course,
                enrollment.getStudent(),
                enrollment.getStatus()
        );
        entity.enrollmentId = enrollment.getEnrollmentId();
        entity.enrolledAt = enrollment.getEnrolledAt();
        entity.canceledAt = enrollment.getCanceledAt();
        return entity;
    }

    public void apply(Enrollment enrollment, CourseJpaEntity course) {
        this.course = course;
        this.student = enrollment.getStudent();
        this.status = enrollment.getStatus();
        this.canceledAt = enrollment.getCanceledAt();
    }

    public Enrollment toDomain() {
        return Enrollment.restore(
                enrollmentId,
                course.toDomain(),
                student,
                status,
                enrolledAt,
                canceledAt
        );
    }

    @PrePersist
    public void prePersist() {
        this.enrolledAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = EnrollmentStatus.ACTIVE;
        }
    }
}
