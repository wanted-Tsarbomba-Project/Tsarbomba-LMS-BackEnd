package com.wanted.codebombalms.inquiry.presentation.api.response;

import com.wanted.codebombalms.inquiry.application.query.AdminInquiryDetail;
import com.wanted.codebombalms.inquiry.domain.model.InquiryDomain;
import com.wanted.codebombalms.inquiry.domain.model.InquirySeverity;
import com.wanted.codebombalms.inquiry.domain.model.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
// 문의 원문과 AI 분석, 관리자 처리 결과 전체를 응답에 담는다.
public class AdminInquiryDetailResponse {

    private Long inquiryId;
    private Long userId;
    private String title;
    private String content;
    private String sourceUrl;
    private String estimatedUrl;
    private String summary;
    private String recommendedAction;
    private InquiryDomain domain;
    private InquirySeverity severity;
    private InquiryStatus status;
    private boolean filtered;
    private String adminReply;
    private Long repliedBy;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;

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
