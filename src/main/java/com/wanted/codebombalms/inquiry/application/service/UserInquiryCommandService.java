package com.wanted.codebombalms.inquiry.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.inquiry.application.command.CorrectionExample;
import com.wanted.codebombalms.inquiry.application.command.CreateInquiryCommand;
import com.wanted.codebombalms.inquiry.application.command.RequestInquiryAnalysisCommand;
import com.wanted.codebombalms.inquiry.application.port.InquiryAnalysisClient;
import com.wanted.codebombalms.inquiry.application.usecase.CreateInquiryUseCase;
import com.wanted.codebombalms.inquiry.application.usecase.HideInquiryReplyUseCase;
import com.wanted.codebombalms.inquiry.domain.exception.InquiryErrorCode;
import com.wanted.codebombalms.inquiry.domain.model.Inquiry;
import com.wanted.codebombalms.inquiry.domain.repository.InquiryAiCorrectionRepository;
import com.wanted.codebombalms.inquiry.domain.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserInquiryCommandService implements CreateInquiryUseCase, HideInquiryReplyUseCase {

    // 운영 데이터가 쌓일수록 AI 분석 컨텍스트가 좋아지도록, 지금 규모에서는 사실상 전체 보정 이력을 보낸다는 의미의 상한이다.
    private static final int MAX_CORRECTION_EXAMPLES_FOR_AI_CONTEXT = 1000;

    private final InquiryRepository inquiryRepository;
    private final InquiryAiCorrectionRepository inquiryAiCorrectionRepository;
    private final InquiryAnalysisClient inquiryAnalysisClient;

    @Override
    // 문의를 등록하고, 트랜잭션이 실제로 커밋된 뒤에 Python AI 분석을 비동기로 요청한다.
    // 사용자 응답은 분석 결과를 기다리지 않는다.
    public Inquiry create(CreateInquiryCommand command) {
        Inquiry inquiry = Inquiry.create(command.userId(), command.content(), command.sourceUrl(), LocalDateTime.now());

        Inquiry saved = inquiryRepository.save(inquiry);

        // 커밋 전에 analyze()(@Async)를 호출하면, Python 응답이 아주 빠르거나 커밋이 지연될 때
        // 분석 결과 반영(findById)이 아직 안 보이는 row를 조회해 NotFoundException이 날 수 있다.
        // 커밋 완료 후로 미뤄서 이 레이스 컨디션을 없앤다.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                requestAiAnalysis(saved);
            }
        });

        return saved;
    }

    private void requestAiAnalysis(Inquiry saved) {
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
