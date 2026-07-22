package com.wanted.codebombalms.inquiry.presentation.api.response;

import com.wanted.codebombalms.inquiry.domain.model.Inquiry;
import com.wanted.codebombalms.inquiry.domain.model.InquiryStatus;

// 문의 등록 결과. AI 분석은 비동기로 진행되므로 등록 시점 상태만 내려준다.
public record InquiryCreateResponse(
        Long inquiryId,
        InquiryStatus status
) {

    public static InquiryCreateResponse from(Inquiry inquiry) {
        return new InquiryCreateResponse(
                inquiry.getInquiryId(),
                inquiry.getStatus()
        );
    }
}
