package com.wanted.codebombalms.inquiry.infrastructure.persistence;

import com.wanted.codebombalms.inquiry.domain.model.InquiryDomain;
import com.wanted.codebombalms.inquiry.domain.model.InquirySeverity;
import com.wanted.codebombalms.inquiry.domain.model.InquiryStatus;

import java.time.LocalDateTime;

// 목록 조회에서 원문 없이 AI 요약 기준 컬럼만 뽑아오는 JPQL constructor projection
public record InquiryListProjection(
        Long inquiryId,
        String title,
        String aiSummary,
        InquiryDomain domain,
        InquirySeverity severity,
        InquiryStatus status,
        boolean filtered,
        LocalDateTime createdAt
) {
}
