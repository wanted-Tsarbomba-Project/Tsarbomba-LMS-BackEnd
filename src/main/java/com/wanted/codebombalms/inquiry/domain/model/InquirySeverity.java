package com.wanted.codebombalms.inquiry.domain.model;

// 문의 심각도
public enum InquirySeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    // Swagger 문서용 값별 의미 설명. @Schema/@Parameter description에서 재사용한다.
    public static final String SCHEMA_DESCRIPTION =
            "문의 심각도 (LOW: 단순 문의/제안, MEDIUM: 일반 오류/불편, "
                    + "HIGH: 기능 사용에 큰 지장, CRITICAL: 로그인/결제/학습 진행 불가급)";
}
