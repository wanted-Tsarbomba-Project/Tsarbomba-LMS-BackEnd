package com.wanted.codebombalms.inquiry.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryErrorCode implements ErrorCode {

    // 조회
    INVALID_PAGE_REQUEST("INQ-001", "페이지 요청 값이 올바르지 않습니다."),
    INQUIRY_NOT_FOUND("INQ-002", "문의를 찾을 수 없습니다."),

    // 분류 수정
    INVALID_CLASSIFICATION_REQUEST("INQ-003", "문의 분류 수정 요청이 올바르지 않습니다."),

    // 필터링 처리/복구
    INVALID_FILTER_REQUEST("INQ-004", "문의 필터링 처리 요청이 올바르지 않습니다."),

    // 답변 등록
    INVALID_REPLY_REQUEST("INQ-005", "문의 답변 등록 요청이 올바르지 않습니다.");

    private final String code;
    private final String message;
}
