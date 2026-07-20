package com.wanted.codebombalms.inquiry.application.command;

// 관리자 필터링 처리/복구 요청 값을 담는다.
public record UpdateInquiryFilterCommand(
        Long inquiryId,
        Long adminId,
        Boolean filtered,
        String reason
) {
}
