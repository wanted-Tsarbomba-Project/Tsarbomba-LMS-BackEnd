package com.wanted.codebombalms.learning.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LearningErrorCode implements ErrorCode {

    LECTURE_NOT_FOUND("LRN-001", "존재하지 않는 강의입니다."),
    COURSE_PROBLEM_STEP_NOT_FOUND("LRN-002", "존재하지 않는 강의 문제 단계입니다."),
    LEARNING_PROGRESS_NOT_FOUND("LRN-003", "학습 진행 기록이 존재하지 않습니다.");

    private final String code;
    private final String message;
}
