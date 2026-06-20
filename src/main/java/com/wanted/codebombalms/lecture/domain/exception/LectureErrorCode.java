package com.wanted.codebombalms.lecture.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LectureErrorCode implements ErrorCode {

    LECTURE_NOT_FOUND("LCT-001", "Lecture was not found."),
    LECTURE_DELETE_STATUS_REQUIRES_DELETE("LCT-002", "Use the delete API to delete a lecture."),
    LECTURE_ORDER_DUPLICATED("LCT-003", "A lecture with the same order already exists."),
    INVALID_YOUTUBE_VIDEO_URL("LCT-004", "Lecture video URL must be a YouTube URL."),
    LECTURE_MATERIAL_NOT_FOUND("LCT-005", "Lecture material was not found."),
    LECTURE_MATERIAL_INVALID_FILE("LCT-006", "Lecture material file is invalid."),
    LECTURE_MATERIAL_UPLOAD_FAILED("LCT-007", "Lecture material upload failed."),
    LECTURE_MATERIAL_DOWNLOAD_URL_FAILED("LCT-008", "Lecture material download URL generation failed."),
    LECTURE_MATERIAL_ACCESS_DENIED("LCT-009", "Lecture material access is denied.");

    private final String code;
    private final String message;
}
