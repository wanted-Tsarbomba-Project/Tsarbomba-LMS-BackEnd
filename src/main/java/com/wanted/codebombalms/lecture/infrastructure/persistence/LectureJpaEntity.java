package com.wanted.codebombalms.lecture.infrastructure.persistence;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureStatus;
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
        name = "lecture",
        indexes = @Index(name = "idx_lecture_course_id", columnList = "course_id")
)
public class LectureJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_id")
    private Long lectureId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "problem_category_id")
    private Long problemCategoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LectureStatus status;

    @Column(name = "lecture_order")
    private Integer lectureOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public LectureJpaEntity(
            Long courseId,
            String title,
            String description,
            String videoUrl,
            String thumbnailUrl,
            Long problemCategoryId,
            LectureStatus status,
            Integer lectureOrder
    ) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.problemCategoryId = problemCategoryId;
        this.status = status;
        this.lectureOrder = lectureOrder;
    }

    public static LectureJpaEntity from(Lecture lecture) {
        LectureJpaEntity entity = new LectureJpaEntity(
                lecture.getCourse().getCourseId(),
                lecture.getTitle(),
                lecture.getDescription(),
                lecture.getVideoUrl(),
                lecture.getThumbnailUrl(),
                lecture.getProblemCategoryId(),
                lecture.getStatus(),
                lecture.getLectureOrder()
        );
        entity.lectureId = lecture.getLectureId();
        entity.createdAt = lecture.getCreatedAt();
        entity.updatedAt = lecture.getUpdatedAt();
        entity.deletedAt = lecture.getDeletedAt();
        return entity;
    }

    public void apply(Lecture lecture) {
        this.courseId = lecture.getCourse().getCourseId();
        this.title = lecture.getTitle();
        this.description = lecture.getDescription();
        this.videoUrl = lecture.getVideoUrl();
        this.thumbnailUrl = lecture.getThumbnailUrl();
        this.problemCategoryId = lecture.getProblemCategoryId();
        this.status = lecture.getStatus();
        this.lectureOrder = lecture.getLectureOrder();
        this.deletedAt = lecture.getDeletedAt();
    }

    public Lecture toDomain(Course course) {
        return Lecture.restore(
                lectureId,
                course,
                title,
                description,
                videoUrl,
                thumbnailUrl,
                problemCategoryId,
                status,
                createdAt,
                updatedAt,
                deletedAt,
                lectureOrder
        );
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = LectureStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
