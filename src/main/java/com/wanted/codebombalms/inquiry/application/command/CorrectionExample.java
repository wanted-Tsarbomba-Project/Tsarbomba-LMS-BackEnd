package com.wanted.codebombalms.inquiry.application.command;

import com.wanted.codebombalms.inquiry.domain.model.InquiryAiCorrection;
import com.wanted.codebombalms.inquiry.domain.model.InquiryCorrectionField;

// Python AI 분석 요청에 함께 실어보낼 관리자 보정 사례 한 건
public record CorrectionExample(
        InquiryCorrectionField fieldName,
        String aiValue,
        String correctedValue,
        String reason
) {

    public static CorrectionExample from(InquiryAiCorrection correction) {
        return new CorrectionExample(
                correction.getFieldName(),
                correction.getAiValue(),
                correction.getCorrectedValue(),
                correction.getReason()
        );
    }
}
