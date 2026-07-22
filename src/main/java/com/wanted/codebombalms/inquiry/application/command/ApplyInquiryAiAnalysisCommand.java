package com.wanted.codebombalms.inquiry.application.command;

import com.wanted.codebombalms.inquiry.domain.model.InquiryDomain;
import com.wanted.codebombalms.inquiry.domain.model.InquirySeverity;

// Python AI 분석 결과를 문의에 반영할 때 사용하는 값을 담는다.
public record ApplyInquiryAiAnalysisCommand(
        Long inquiryId,
        String title,
        String aiSummary,
        InquirySeverity severity,
        InquiryDomain domain,
        String estimatedUrl,
        String aiRecommendedAction,
        boolean filtered
) {
}
