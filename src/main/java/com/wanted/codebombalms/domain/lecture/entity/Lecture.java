package com.wanted.codebombalms.domain.lecture.entity;

import com.wanted.codebombalms.domain.course.entity.Course;
import com.wanted.codebombalms.domain.lecture.enums.LectureStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "lecture")
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_id")
    private Long lectureId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @ToString.Exclude
    private Course course;

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

    public static Lecture create(
            Course course,
            String title,
            String description,
            String videoUrl,
            String thumbnailUrl,
            Integer lectureOrder,
            LectureStatus status
    ) {
        Lecture lecture = new Lecture();
        lecture.setCourse(course);
        lecture.setTitle(title);
        lecture.setDescription(description);
        lecture.setVideoUrl(videoUrl);
        lecture.setThumbnailUrl(thumbnailUrl);
        lecture.setLectureOrder(lectureOrder);
        lecture.setStatus(status);

        return lecture;
    }

    public void update(
            String title,
            String description,
            String videoUrl,
            String thumbnailUrl,
            Integer lectureOrder,
            LectureStatus status
    ) {
        if (title != null) {
            this.title = title;
        }

        if (description != null) {
            this.description = description;
        }

        if (videoUrl != null) {
            this.videoUrl = videoUrl;
        }

        if (thumbnailUrl != null) {
            this.thumbnailUrl = thumbnailUrl;
        }

        if (lectureOrder != null) {
            this.lectureOrder = lectureOrder;
        }

        if (status != null) {
            this.status = status;
        }
    }

    public void delete() {
        this.status = LectureStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
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