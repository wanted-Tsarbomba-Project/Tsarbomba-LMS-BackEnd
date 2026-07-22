package com.wanted.codebombalms.inquiry.application.usecase;

import com.wanted.codebombalms.inquiry.application.command.CreateInquiryCommand;
import com.wanted.codebombalms.inquiry.domain.model.Inquiry;

// 사용자가 새 문의를 등록하는 유스케이스 진입점이다.
public interface CreateInquiryUseCase {

    Inquiry create(CreateInquiryCommand command);
}
