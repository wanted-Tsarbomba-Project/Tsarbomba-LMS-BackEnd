package com.wanted.codebombalms.inquiry.application.usecase;

import com.wanted.codebombalms.inquiry.domain.model.Inquiry;

public interface HideInquiryReplyUseCase {

    Inquiry hideReply(Long inquiryId, Long userId);
}
