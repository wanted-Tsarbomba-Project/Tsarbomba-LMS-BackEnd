package com.wanted.codebombalms.inquiry.presentation.api.request;

import com.wanted.codebombalms.inquiry.application.command.ReplyInquiryCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

// 관리자 문의 답변 등록 요청을 받는다.
public record InquiryReplyRequest(

        @Schema(description = "답변 내용", example = "문의 주신 문제 제출 오류를 확인했고 수정 반영했습니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "답변 내용은 필수입니다.")
        String content
) {

    // path variable의 문의 ID와 인증된 관리자 ID를 더해 command로 변환한다.
    public ReplyInquiryCommand toCommand(Long inquiryId, Long adminId) {
        return new ReplyInquiryCommand(inquiryId, adminId, content);
    }
}
