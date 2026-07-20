package com.wanted.codebombalms.inquiry.presentation.api.response;

import com.wanted.codebombalms.inquiry.domain.model.Inquiry;

// 필터링 처리/복구 결과로 바뀐 상태만 응답에 담는다.
public record InquiryFilterUpdateResponse(
        Long inquiryId,
        boolean filtered
) {

    public static InquiryFilterUpdateResponse from(Inquiry inquiry) {
        return new InquiryFilterUpdateResponse(
                inquiry.getInquiryId(),
                inquiry.isFiltered()
        );
    }
}
