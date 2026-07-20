package com.wanted.codebombalms.inquiry.application.usecase;

import com.wanted.codebombalms.inquiry.application.command.ApplyInquiryAiAnalysisCommand;
import com.wanted.codebombalms.inquiry.domain.model.Inquiry;

// Python AI 분석 결과를 문의에 반영하는 유스케이스 진입점이다.
public interface ApplyInquiryAiAnalysisUseCase {

    Inquiry applyAiAnalysis(ApplyInquiryAiAnalysisCommand command);
}
