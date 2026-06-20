package com.wanted.codebombalms.lecture.presentation.api.response;

import com.wanted.codebombalms.lecture.domain.model.LectureMaterial;
import java.time.LocalDateTime;

public record LectureMaterialResponse(
        Long lectureMaterialId,
        Long lectureId,
        String originalFileName,
        String contentType,
        Long fileSize,
        LocalDateTime createdAt
) {

    public static LectureMaterialResponse from(LectureMaterial material) {
        return new LectureMaterialResponse(
                material.getLectureMaterialId(),
                material.getLectureId(),
                material.getOriginalFileName(),
                material.getContentType(),
                material.getFileSize(),
                material.getCreatedAt()
        );
    }
}
