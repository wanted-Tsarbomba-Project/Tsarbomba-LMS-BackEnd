package com.wanted.codebombalms.inquiry.application.command;

// 사용자 문의 등록 요청 값을 담는다.
public record CreateInquiryCommand(
        Long userId,
        String content,
        String sourceUrl
) {
}
