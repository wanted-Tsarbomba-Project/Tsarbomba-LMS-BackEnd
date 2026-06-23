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
    PROBLEM_NOT_FOUND("LRN-007", "Problem was not found."),
    LECTURE_PROBLEM_NOT_UNLOCKED("LRN-008", "Lecture problem is not unlocked."),
    LECTURE_PROBLEM_SET_ALREADY_COMPLETED("LRN-009", "Lecture problem set is already completed."),
    INVALID_LECTURE_PROGRESS("LRN-010", "Lecture progress value is invalid."),
    LECTURE_PROGRESS_ACCESS_DENIED("LRN-011", "Only enrolled students can access lecture learning content.");

    private final String code;
    private final String message;
}
