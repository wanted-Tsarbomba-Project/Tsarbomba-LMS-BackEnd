package com.wanted.codebombalms.inquiry.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 관리자가 AI 분석 결과를 보정할 수 있는 inquiry 컬럼
@Getter
@RequiredArgsConstructor
public enum InquiryCorrectionField {

    DOMAIN("domain"),
    SEVERITY("severity"),
    IS_FILTERED("is_filtered");

    private final String fieldName;
}
