package com.wanted.codebombalms.inquiry.presentation.api.response;

import com.wanted.codebombalms.inquiry.application.query.ActiveInquiryReply;

import java.time.LocalDateTime;

// 로그인 첫 화면에서 모달로 보여줄 문의 답변 한 건
public record ActiveInquiryReplyResponse(
        Long inquiryId,
        String title,
        String content,
        String adminReply,
        LocalDateTime repliedAt
) {

    public static ActiveInquiryReplyResponse from(ActiveInquiryReply reply) {
        return new ActiveInquiryReplyResponse(
                reply.inquiryId(),
                reply.title(),
                reply.content(),
                reply.adminReply(),
                reply.repliedAt()
        );
    }
}
