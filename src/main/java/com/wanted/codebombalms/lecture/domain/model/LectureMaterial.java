package com.wanted.codebombalms.lecture.domain.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class LectureMaterial {

    private Long lectureMaterialId;
    private Long lectureId;
    private String originalFileName;
    private String storedFileName;
    private String filePath;
    private String contentType;
    private Long fileSize;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    private LectureMaterial(
            Long lectureMaterialId,
            Long lectureId,
            String originalFileName,
            String storedFileName,
            String filePath,
            String contentType,
            Long fileSize,
            LocalDateTime createdAt,
            LocalDateTime deletedAt
    ) {
        this.lectureMaterialId = lectureMaterialId;
        this.lectureId = lectureId;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.filePath = filePath;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public static LectureMaterial create(
            Long lectureId,
            String originalFileName,
            String storedFileName,
            String filePath,
            String contentType,
            Long fileSize
    ) {
        return new LectureMaterial(
                null,
                lectureId,
                originalFileName,
                storedFileName,
                filePath,
                contentType,
                fileSize,
                null,
                null
        );
    }

    public static LectureMaterial restore(
            Long lectureMaterialId,
            Long lectureId,
            String originalFileName,
            String storedFileName,
            String filePath,
            String contentType,
            Long fileSize,
            LocalDateTime createdAt,
            LocalDateTime deletedAt
    ) {
        return new LectureMaterial(
                lectureMaterialId,
                lectureId,
                originalFileName,
                storedFileName,
                filePath,
                contentType,
                fileSize,
                createdAt,
                deletedAt
        );
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
