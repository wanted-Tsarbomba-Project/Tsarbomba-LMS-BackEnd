package com.wanted.codebombalms.inquiry.application.query;

import com.wanted.codebombalms.inquiry.domain.model.InquiryDomain;
import com.wanted.codebombalms.inquiry.domain.model.InquirySeverity;
import com.wanted.codebombalms.inquiry.domain.model.InquiryStatus;

import java.time.LocalDateTime;

// 관리자 문의 목록 한 행에 필요한 AI 요약 기준 정보만 담는다.
public record AdminInquiryListItem(
        Long inquiryId,
        String title,
        String summary,
        InquiryDomain domain,
        InquirySeverity severity,
        InquiryStatus status,
        boolean filtered,
        LocalDateTime createdAt
) {
}
