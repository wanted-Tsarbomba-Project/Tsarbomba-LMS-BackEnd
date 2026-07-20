package com.wanted.codebombalms.inquiry.presentation.api.request;

import com.wanted.codebombalms.inquiry.application.command.UpdateInquiryFilterCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 관리자 문의 필터링 처리/복구 요청을 받는다.
public record InquiryFilterUpdateRequest(

        @Schema(description = "true면 필터링(스팸/무의미) 처리, false면 정상 목록으로 복구", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "필터링 여부는 필수입니다.")
        Boolean filtered,

        @Schema(description = "필터링 처리/복구 사유 (필수)", example = "AI는 무의미 문의로 판단했지만 실제 문제 제출 오류 문의로 확인됨", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "처리 사유는 필수입니다.")
        String reason
) {

    // path variable의 문의 ID와 인증된 관리자 ID를 더해 command로 변환한다.
    public UpdateInquiryFilterCommand toCommand(Long inquiryId, Long adminId) {
        return new UpdateInquiryFilterCommand(inquiryId, adminId, filtered, reason);
    }
}
