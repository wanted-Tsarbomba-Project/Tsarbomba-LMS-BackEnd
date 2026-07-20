package com.wanted.codebombalms.inquiry.presentation.api.response;

import com.wanted.codebombalms.inquiry.domain.model.Inquiry;
import com.wanted.codebombalms.inquiry.domain.model.InquiryDomain;
import com.wanted.codebombalms.inquiry.domain.model.InquirySeverity;

// 분류 수정 결과로 바뀐 도메인/심각도만 응답에 담는다.
public record InquiryClassificationUpdateResponse(
        Long inquiryId,
        InquiryDomain domain,
        InquirySeverity severity
) {

    public static InquiryClassificationUpdateResponse from(Inquiry inquiry) {
        return new InquiryClassificationUpdateResponse(
                inquiry.getInquiryId(),
                inquiry.getDomain(),
                inquiry.getSeverity()
        );
    }
}
