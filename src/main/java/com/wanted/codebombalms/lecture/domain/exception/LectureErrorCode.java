package com.wanted.codebombalms.lecture.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LectureErrorCode implements ErrorCode {

    LECTURE_NOT_FOUND("LCT-001", "Lecture was not found."),
    LECTURE_DELETE_STATUS_REQUIRES_DELETE("LCT-002", "Use the delete API to delete a lecture."),
    LECTURE_ORDER_DUPLICATED("LCT-003", "A lecture with the same order already exists.");

    private final String code;
    private final String message;
}
