package com.wanted.codebombalms.inquiry.application.query;

import com.wanted.codebombalms.inquiry.domain.model.InquiryDomain;
import com.wanted.codebombalms.inquiry.domain.model.InquirySeverity;
import com.wanted.codebombalms.inquiry.domain.model.InquiryStatus;

import java.time.LocalDateTime;

// 관리자 문의 상세 화면에 필요한 원문/AI 분석/처리 결과 전체를 담는다.
public record AdminInquiryDetail(
        Long inquiryId,
        Long userId,
        String title,
        String content,
        String sourceUrl,
        String estimatedUrl,
        String summary,
        String recommendedAction,
        InquiryDomain domain,
        InquirySeverity severity,
        InquiryStatus status,
        boolean filtered,
        String adminReply,
        Long repliedBy,
        LocalDateTime repliedAt,
        LocalDateTime createdAt
) {
}
