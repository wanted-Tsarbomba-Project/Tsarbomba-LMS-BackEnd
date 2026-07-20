package com.wanted.codebombalms.inquiry.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.inquiry.application.command.ApplyInquiryAiAnalysisCommand;
import com.wanted.codebombalms.inquiry.application.command.CorrectionExample;
import com.wanted.codebombalms.inquiry.application.command.CreateInquiryCommand;
import com.wanted.codebombalms.inquiry.application.command.RequestInquiryAnalysisCommand;
import com.wanted.codebombalms.inquiry.application.port.InquiryAnalysisClient;
import com.wanted.codebombalms.inquiry.application.usecase.ApplyInquiryAiAnalysisUseCase;
import com.wanted.codebombalms.inquiry.application.usecase.CreateInquiryUseCase;
import com.wanted.codebombalms.inquiry.application.usecase.HideInquiryReplyUseCase;
import com.wanted.codebombalms.inquiry.domain.exception.InquiryErrorCode;
import com.wanted.codebombalms.inquiry.domain.model.Inquiry;
import com.wanted.codebombalms.inquiry.domain.repository.InquiryAiCorrectionRepository;
import com.wanted.codebombalms.inquiry.domain.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserInquiryCommandService
        implements CreateInquiryUseCase, HideInquiryReplyUseCase, ApplyInquiryAiAnalysisUseCase {

    // 운영 데이터가 쌓일수록 AI 분석 컨텍스트가 좋아지도록, 지금 규모에서는 사실상 전체 보정 이력을 보낸다는 의미의 상한이다.
    private static final int MAX_CORRECTION_EXAMPLES_FOR_AI_CONTEXT = 1000;

    private final InquiryRepository inquiryRepository;
    private final InquiryAiCorrectionRepository inquiryAiCorrectionRepository;
    private final InquiryAnalysisClient inquiryAnalysisClient;

    @Override
    // 문의를 등록하고 저장이 끝난 뒤 Python AI 분석을 비동기로 요청한다. 사용자 응답은 분석 결과를 기다리지 않는다.
    public Inquiry create(CreateInquiryCommand command) {
        Inquiry inquiry = Inquiry.create(command.userId(), command.content(), command.sourceUrl(), LocalDateTime.now());

        Inquiry saved = inquiryRepository.save(inquiry);

        List<CorrectionExample> correctionExamples = inquiryAiCorrectionRepository
                .findRecentCorrections(MAX_CORRECTION_EXAMPLES_FOR_AI_CONTEXT)
                .stream()
                .map(CorrectionExample::from)
                .toList();

        inquiryAnalysisClient.analyze(new RequestInquiryAnalysisCommand(
                saved.getInquiryId(),
                saved.getUserId(),
                saved.getSourceUrl(),
                saved.getContent(),
                correctionExamples
        ));

        return saved;
    }

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

    @Override
    // 본인 문의 답변을 로그인 첫 화면에서 비노출 처리한다.
    public Inquiry hideReply(Long inquiryId, Long userId) {
        Inquiry inquiry = inquiryRepository.findByIdAndUserId(inquiryId, userId)
                .orElseThrow(() -> new NotFoundException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        inquiry.hideReply(LocalDateTime.now());

        return inquiryRepository.save(inquiry);
    }
}
