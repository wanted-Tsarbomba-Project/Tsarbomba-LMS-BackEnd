package com.wanted.codebombalms.lecture.domain.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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

    public static LectureMaterial create(
            Long lectureId,
            String originalFileName,
            String storedFileName,
            String filePath,
            String contentType,
            Long fileSize
    ) {
        LectureMaterial material = new LectureMaterial();
        material.setLectureId(lectureId);
        material.setOriginalFileName(originalFileName);
        material.setStoredFileName(storedFileName);
        material.setFilePath(filePath);
        material.setContentType(contentType);
        material.setFileSize(fileSize);
        return material;
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
