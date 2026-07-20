package com.wanted.codebombalms.inquiry.application.query;

import java.time.LocalDateTime;

// 로그인 첫 화면에 띄울 미확인 문의 답변 정보
public record ActiveInquiryReply(
        Long inquiryId,
        String title,
        String content,
        String adminReply,
        LocalDateTime repliedAt
) {
}
