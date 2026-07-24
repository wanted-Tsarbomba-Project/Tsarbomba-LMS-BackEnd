package com.wanted.codebombalms.inquiry.application.usecase;

import com.wanted.codebombalms.inquiry.application.query.AdminInquiryDetail;

public interface GetAdminInquiryDetailUseCase {

    // 문의 ID로 상세 정보를 조회한다.
    AdminInquiryDetail getInquiryDetail(Long inquiryId);
}
