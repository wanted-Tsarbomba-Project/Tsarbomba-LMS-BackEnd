package com.wanted.codebombalms.inquiry.presentation.api.request;

import com.wanted.codebombalms.inquiry.application.command.UpdateInquiryClassificationCommand;
import com.wanted.codebombalms.inquiry.domain.model.InquiryDomain;
import com.wanted.codebombalms.inquiry.domain.model.InquirySeverity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
// 관리자 문의 분류(도메인/심각도) 수정 요청을 받는다.
public class InquiryClassificationUpdateRequest {

    @Schema(description = InquiryDomain.SCHEMA_DESCRIPTION, example = "PROBLEMS")
    private InquiryDomain domain;

    @Schema(description = InquirySeverity.SCHEMA_DESCRIPTION, example = "HIGH")
    private InquirySeverity severity;

    @Schema(description = "분류 수정 사유 (필수)", example = "문제 제출 결과 미노출은 강의가 아니라 문제 도메인 이슈로 봐야 함")
    private String reason;

    // path variable의 문의 ID와 인증된 관리자 ID를 더해 command로 변환한다.
    public UpdateInquiryClassificationCommand toCommand(Long inquiryId, Long adminId) {
        return new UpdateInquiryClassificationCommand(inquiryId, adminId, domain, severity, reason);
    }
}
