package com.wanted.codebombalms.inquiry.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.inquiry.application.usecase.HideInquiryReplyUseCase;
import com.wanted.codebombalms.inquiry.domain.exception.InquiryErrorCode;
import com.wanted.codebombalms.inquiry.domain.model.Inquiry;
import com.wanted.codebombalms.inquiry.domain.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class UserInquiryCommandService implements HideInquiryReplyUseCase {

    private final InquiryRepository inquiryRepository;

    @Override
    // 본인 문의 답변을 로그인 첫 화면에서 비노출 처리한다.
    public Inquiry hideReply(Long inquiryId, Long userId) {
        Inquiry inquiry = inquiryRepository.findByIdAndUserId(inquiryId, userId)
                .orElseThrow(() -> new NotFoundException(InquiryErrorCode.INQUIRY_NOT_FOUND));

        inquiry.hideReply(LocalDateTime.now());

        return inquiryRepository.save(inquiry);
    }
}
