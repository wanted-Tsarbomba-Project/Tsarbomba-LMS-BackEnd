package com.wanted.codebombalms.lecture.domain.model;

import com.wanted.codebombalms.course.domain.model.Course;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Lecture {

    private Long lectureId;
    private Course course;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    private LectureStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
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
        lecture.setStatus(status == null ? LectureStatus.ACTIVE : status);

        return lecture;
    }

    public static Lecture restore(
            Long lectureId,
            Course course,
            String title,
            String description,
            String videoUrl,
            String thumbnailUrl,
            LectureStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt,
            Integer lectureOrder
    ) {
        return new Lecture(
                lectureId,
                course,
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
}
