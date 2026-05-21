package com.wanted.codebombalms.domain.lecture.infrastructure.persistence;

import com.wanted.codebombalms.domain.course.infrastructure.persistence.CourseJpaEntity;
import com.wanted.codebombalms.domain.lecture.domain.model.Lecture;
import com.wanted.codebombalms.domain.lecture.domain.model.LectureStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@ToString
@Table(name = "lecture")
public class LectureJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_id")
    private Long lectureId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @ToString.Exclude
    private CourseJpaEntity course;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LectureStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "lecture_order", nullable = false)
    private Integer lectureOrder;

    public LectureJpaEntity(
            CourseJpaEntity course,
            String title,
            String description,
            String videoUrl,
            String thumbnailUrl,
            LectureStatus status,
            Integer lectureOrder
    ) {
        this.course = course;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.status = status;
        this.lectureOrder = lectureOrder;
    }

    public static LectureJpaEntity from(Lecture lecture, CourseJpaEntity course) {
        LectureJpaEntity entity = new LectureJpaEntity(
                course,
                lecture.getTitle(),
                lecture.getDescription(),
                lecture.getVideoUrl(),
                lecture.getThumbnailUrl(),
                lecture.getStatus(),
                lecture.getLectureOrder()
        );
        entity.lectureId = lecture.getLectureId();
        entity.createdAt = lecture.getCreatedAt();
        entity.updatedAt = lecture.getUpdatedAt();
        entity.deletedAt = lecture.getDeletedAt();
        return entity;
    }

    public void apply(Lecture lecture, CourseJpaEntity course) {
        this.course = course;
        this.title = lecture.getTitle();
        this.description = lecture.getDescription();
        this.videoUrl = lecture.getVideoUrl();
        this.thumbnailUrl = lecture.getThumbnailUrl();
        this.status = lecture.getStatus();
        this.lectureOrder = lecture.getLectureOrder();
        this.deletedAt = lecture.getDeletedAt();
    }

    public Lecture toDomain() {
        return Lecture.restore(
                lectureId,
                course.toDomain(),
                title,
                description,
                videoUrl,
                thumbnailUrl,
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
