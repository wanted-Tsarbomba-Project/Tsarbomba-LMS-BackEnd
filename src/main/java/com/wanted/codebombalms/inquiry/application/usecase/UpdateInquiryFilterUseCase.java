package com.wanted.codebombalms.inquiry.application.usecase;

import com.wanted.codebombalms.inquiry.application.command.UpdateInquiryFilterCommand;
import com.wanted.codebombalms.inquiry.domain.model.Inquiry;

// 관리자가 문의 필터링 처리 또는 복구를 수행하는 유스케이스 진입점이다.
public interface UpdateInquiryFilterUseCase {

    Inquiry updateFilter(UpdateInquiryFilterCommand command);
}
