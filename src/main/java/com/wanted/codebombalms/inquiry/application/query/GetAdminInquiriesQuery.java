package com.wanted.codebombalms.inquiry.application.query;

import com.wanted.codebombalms.inquiry.domain.model.InquiryDomain;
import com.wanted.codebombalms.inquiry.domain.model.InquirySeverity;
import com.wanted.codebombalms.inquiry.domain.model.InquiryStatus;

// 관리자 문의 목록 조회 조건을 담는다.
public record GetAdminInquiriesQuery(
        boolean isFiltered,
        InquiryDomain domain,
        InquirySeverity severity,
        InquiryStatus status,
        int page,
        int size
) {
}
