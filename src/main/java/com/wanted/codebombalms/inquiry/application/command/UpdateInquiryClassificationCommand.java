package com.wanted.codebombalms.inquiry.application.command;

import com.wanted.codebombalms.inquiry.domain.model.InquiryDomain;
import com.wanted.codebombalms.inquiry.domain.model.InquirySeverity;

// 관리자 분류(도메인/심각도) 수정 요청 값을 담는다.
public record UpdateInquiryClassificationCommand(
        Long inquiryId,
        Long adminId,
        InquiryDomain domain,
        InquirySeverity severity,
        String reason
) {
}
