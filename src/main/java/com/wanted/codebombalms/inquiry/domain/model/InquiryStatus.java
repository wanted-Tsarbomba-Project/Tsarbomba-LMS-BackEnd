package com.wanted.codebombalms.inquiry.domain.model;

// 문의 처리 상태
public enum InquiryStatus {
    OPEN,
    ANSWERED;

    // Swagger 문서용 값별 의미 설명. @Schema/@Parameter description에서 재사용한다.
    public static final String SCHEMA_DESCRIPTION = "문의 처리 상태 (OPEN: 새 문의, ANSWERED: 답변 완료)";
}
