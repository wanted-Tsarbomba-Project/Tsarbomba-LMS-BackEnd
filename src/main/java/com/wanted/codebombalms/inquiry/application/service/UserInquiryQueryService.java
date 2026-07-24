package com.wanted.codebombalms.inquiry.application.service;

import com.wanted.codebombalms.inquiry.application.query.ActiveInquiryReply;
import com.wanted.codebombalms.inquiry.application.query.InquiryQueryRepository;
import com.wanted.codebombalms.inquiry.application.usecase.GetActiveInquiryRepliesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserInquiryQueryService implements GetActiveInquiryRepliesUseCase {

    private final InquiryQueryRepository inquiryQueryRepository;

    @Override
    // 로그인 첫 화면에 노출할 답변 확인 전 문의 답변 목록을 조회한다.
    public List<ActiveInquiryReply> getActiveReplies(Long userId) {
        return inquiryQueryRepository.findActiveRepliesByUserId(userId);
    }
}
