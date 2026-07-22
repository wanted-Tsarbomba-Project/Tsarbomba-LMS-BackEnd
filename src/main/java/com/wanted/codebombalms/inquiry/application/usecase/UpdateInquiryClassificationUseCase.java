package com.wanted.codebombalms.inquiry.application.usecase;

import com.wanted.codebombalms.inquiry.application.command.UpdateInquiryClassificationCommand;
import com.wanted.codebombalms.inquiry.domain.model.Inquiry;

// 관리자가 문의의 도메인/심각도 분류를 보정하는 유스케이스 진입점이다.
public interface UpdateInquiryClassificationUseCase {

    Inquiry updateClassification(UpdateInquiryClassificationCommand command);
}
