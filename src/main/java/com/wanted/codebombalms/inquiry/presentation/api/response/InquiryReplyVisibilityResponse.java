package com.wanted.codebombalms.inquiry.presentation.api.response;

import com.wanted.codebombalms.inquiry.domain.model.Inquiry;

// 답변 확인 후 노출 상태 변경 결과
public record InquiryReplyVisibilityResponse(
        Long inquiryId,
        boolean replyVisible
) {

    public static InquiryReplyVisibilityResponse from(Inquiry inquiry) {
        return new InquiryReplyVisibilityResponse(
                inquiry.getInquiryId(),
                inquiry.isReplyVisible()
        );
    }
}
