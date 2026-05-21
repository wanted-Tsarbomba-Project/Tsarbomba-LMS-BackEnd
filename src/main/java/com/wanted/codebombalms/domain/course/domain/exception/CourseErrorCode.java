package com.wanted.codebombalms.domain.course.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourseErrorCode implements ErrorCode {

    COURSE_NOT_FOUND(404, "CR-001", "존재하지 않는 강좌입니다."),
    COURSE_LECTURE_REQUIRED(400, "CR-002", "강좌를 개설하려면 강의가 1개 이상 필요합니다."),
    COURSE_ACTIVE_STATUS_REQUIRES_PUBLISH(400, "CR-003", "강좌 활성화는 개설 기능을 통해서만 가능합니다."),
    COURSE_NOT_PUBLISHABLE_STATUS(400, "CR-004", "작성 중인 강좌만 개설할 수 있습니다.");

    private final int status;
    private final String code;
    private final String message;
}
