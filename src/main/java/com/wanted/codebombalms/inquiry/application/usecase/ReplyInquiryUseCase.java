package com.wanted.codebombalms.inquiry.application.usecase;

import com.wanted.codebombalms.inquiry.application.command.ReplyInquiryCommand;
import com.wanted.codebombalms.inquiry.domain.model.Inquiry;

// 관리자가 문의에 답변을 등록하는 유스케이스 진입점이다.
public interface ReplyInquiryUseCase {

    Inquiry reply(ReplyInquiryCommand command);
}
