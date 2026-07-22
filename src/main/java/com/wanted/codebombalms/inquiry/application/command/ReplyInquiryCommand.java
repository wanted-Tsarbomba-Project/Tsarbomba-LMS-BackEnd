package com.wanted.codebombalms.inquiry.application.command;

// 관리자 답변 등록 요청 값을 담는다.
public record ReplyInquiryCommand(
        Long inquiryId,
        Long adminId,
        String content
) {
}
