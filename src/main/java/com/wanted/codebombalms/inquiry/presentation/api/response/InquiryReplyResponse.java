package com.wanted.codebombalms.inquiry.presentation.api.response;

import com.wanted.codebombalms.inquiry.domain.model.Inquiry;
import com.wanted.codebombalms.inquiry.domain.model.InquiryStatus;

import java.time.LocalDateTime;

// 답변 등록 결과로 바뀐 상태와 답변 내용을 응답에 담는다.
public record InquiryReplyResponse(
        Long inquiryId,
        InquiryStatus status,
        String adminReply,
        Long repliedBy,
        LocalDateTime repliedAt
) {

    public static InquiryReplyResponse from(Inquiry inquiry) {
        return new InquiryReplyResponse(
                inquiry.getInquiryId(),
                inquiry.getStatus(),
                inquiry.getAdminReply(),
                inquiry.getRepliedBy(),
                inquiry.getRepliedAt()
        );
    }
}
