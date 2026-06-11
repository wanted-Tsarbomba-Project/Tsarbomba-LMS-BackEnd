package com.wanted.codebombalms.learning.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LearningErrorCode implements ErrorCode {

    LECTURE_NOT_FOUND("LRN-001", "Lecture was not found."),
    COURSE_PROBLEM_STEP_NOT_FOUND("LRN-002", "Course problem step was not found."),
    LEARNING_PROGRESS_NOT_FOUND("LRN-003", "Learning progress was not found."),
    COURSE_NOT_FOUND("LRN-004", "Course was not found."),
    LECTURE_PROBLEM_SET_NOT_FOUND("LRN-005", "Lecture problem set was not found."),
    PROBLEM_NOT_IN_LECTURE_PROBLEM_SET("LRN-006", "Problem does not belong to the lecture problem set."),
    PROBLEM_NOT_FOUND("LRN-007", "Problem was not found.");

    private final String code;
    private final String message;
}
