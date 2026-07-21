package com.wanted.codebombalms.inquiry.application.command;

import java.util.List;

// Python AI 분석 서버에 전달할 문의 원문 정보와 관리자 보정 학습 컨텍스트를 담는다.
public record RequestInquiryAnalysisCommand(
        Long inquiryId,
        Long userId,
        String sourceUrl,
        String content,
        List<CorrectionExample> correctionExamples
) {
}
