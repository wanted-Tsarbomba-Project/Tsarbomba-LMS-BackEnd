package com.wanted.codebombalms.inquiry.domain.repository;

import com.wanted.codebombalms.inquiry.domain.model.InquiryAiCorrection;
import com.wanted.codebombalms.inquiry.domain.model.InquiryCorrectionField;

import java.util.List;
import java.util.Optional;

public interface InquiryAiCorrectionRepository {

    // inquiry_id + field_name 기준으로 마지막 보정 row를 조회한다.
    Optional<InquiryAiCorrection> findByInquiryIdAndFieldName(Long inquiryId, InquiryCorrectionField fieldName);

    // AI 분석 컨텍스트로 전달할 최근 보정 사례를 최신순으로 최대 limit건 조회한다.
    List<InquiryAiCorrection> findRecentCorrections(int limit);

    InquiryAiCorrection save(InquiryAiCorrection correction);
}
