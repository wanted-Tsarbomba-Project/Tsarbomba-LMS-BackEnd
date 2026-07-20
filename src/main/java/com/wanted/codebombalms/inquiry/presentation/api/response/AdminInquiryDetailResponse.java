package com.wanted.codebombalms.inquiry.presentation.api.response;

import com.wanted.codebombalms.inquiry.application.query.AdminInquiryDetail;
import com.wanted.codebombalms.inquiry.domain.model.InquiryDomain;
import com.wanted.codebombalms.inquiry.domain.model.InquirySeverity;
import com.wanted.codebombalms.inquiry.domain.model.InquiryStatus;

import java.time.LocalDateTime;

// 문의 원문과 AI 분석, 관리자 처리 결과 전체를 응답에 담는다.
public record AdminInquiryDetailResponse(
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

    public static AdminInquiryDetailResponse from(AdminInquiryDetail detail) {
        return new AdminInquiryDetailResponse(
                detail.inquiryId(),
                detail.userId(),
                detail.title(),
                detail.content(),
                detail.sourceUrl(),
                detail.estimatedUrl(),
                detail.summary(),
                detail.recommendedAction(),
                detail.domain(),
                detail.severity(),
                detail.status(),
                detail.filtered(),
                detail.adminReply(),
                detail.repliedBy(),
                detail.repliedAt(),
                detail.createdAt()
        );
    }
}
