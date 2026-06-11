package com.wanted.codebombalms.enrollment.infrastructure.persistence;

import com.wanted.codebombalms.enrollment.domain.model.Enrollment;
import com.wanted.codebombalms.enrollment.domain.model.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@ToString
@Table(
        name = "enrollment",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_enrollment_user_course",
                columnNames = {"user_id", "course_id"}
        )
)
public class EnrollmentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EnrollmentStatus status;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDateTime enrolledAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    public EnrollmentJpaEntity(
            Long userId,
            Long courseId,
            EnrollmentStatus status
    ) {
        this.userId = userId;
        this.courseId = courseId;
        this.status = status;
    }

    public static EnrollmentJpaEntity from(Enrollment enrollment) {
        EnrollmentJpaEntity entity = new EnrollmentJpaEntity(
                enrollment.getUserId(),
                enrollment.getCourseId(),
                enrollment.getStatus()
        );
        entity.enrollmentId = enrollment.getEnrollmentId();
        entity.enrolledAt = enrollment.getEnrolledAt();
        entity.canceledAt = enrollment.getCanceledAt();
        return entity;
    }

    public void apply(Enrollment enrollment) {
        this.userId = enrollment.getUserId();
        this.courseId = enrollment.getCourseId();
        this.status = enrollment.getStatus();
        this.enrolledAt = enrollment.getEnrolledAt();
        this.canceledAt = enrollment.getCanceledAt();
    }

    public Enrollment toDomain() {
        return Enrollment.restore(
                enrollmentId,
                userId,
                courseId,
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
