package com.wanted.codebombalms.inquiry.domain.repository;

import com.wanted.codebombalms.inquiry.domain.model.InquiryAiCorrection;
import com.wanted.codebombalms.inquiry.domain.model.InquiryCorrectionField;

import java.util.Optional;

public interface InquiryAiCorrectionRepository {

    // inquiry_id + field_name 기준으로 마지막 보정 row를 조회한다.
    Optional<InquiryAiCorrection> findByInquiryIdAndFieldName(Long inquiryId, InquiryCorrectionField fieldName);

    InquiryAiCorrection save(InquiryAiCorrection correction);
}
