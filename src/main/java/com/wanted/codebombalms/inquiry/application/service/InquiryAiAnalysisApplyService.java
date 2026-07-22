package com.wanted.codebombalms.inquiry.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.inquiry.application.command.ApplyInquiryAiAnalysisCommand;
import com.wanted.codebombalms.inquiry.application.usecase.ApplyInquiryAiAnalysisUseCase;
import com.wanted.codebombalms.inquiry.domain.exception.InquiryErrorCode;
import com.wanted.codebombalms.inquiry.domain.model.Inquiry;
import com.wanted.codebombalms.inquiry.domain.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// UserInquiryCommandService와 별도 빈으로 분리했다.
// FastApiInquiryAnalysisClient가 이 유스케이스를 호출하는데, UserInquiryCommandService가
// FastApiInquiryAnalysisClient(InquiryAnalysisClient)를 의존하고 있어서 같은 클래스에 두면
// UserInquiryCommandService -> FastApiInquiryAnalysisClient -> UserInquiryCommandService
// 순환 참조가 생긴다.
@Service
@RequiredArgsConstructor
@Transactional
public class InquiryAiAnalysisApplyService implements ApplyInquiryAiAnalysisUseCase {

    private final InquiryRepository inquiryRepository;

    @Override
    // Python AI 분석 응답을 받아 문의의 분류/요약/추정 URL/필터링 여부를 반영한다.
    public Inquiry applyAiAnalysis(ApplyInquiryAiAnalysisCommand command) {
        Inquiry inquiry = inquiryRepository.findById(command.inquiryId())
                .orElseThrow(() -> new NotFoundException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        inquiry.applyAiAnalysis(
                command.title(),
                command.aiSummary(),
                command.severity(),
                command.domain(),
                command.estimatedUrl(),
                command.aiRecommendedAction(),
                command.filtered(),
                LocalDateTime.now()
        );

        return inquiryRepository.save(inquiry);
    }
}
