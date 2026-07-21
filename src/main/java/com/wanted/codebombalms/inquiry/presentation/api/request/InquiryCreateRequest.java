package com.wanted.codebombalms.inquiry.presentation.api.request;

import com.wanted.codebombalms.inquiry.application.command.CreateInquiryCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 사용자 문의 등록 요청을 받는다.
public record InquiryCreateRequest(

        @Schema(description = "문의 원문", example = "문제 제출했는데 계속 로딩만 되고 결과가 안 나와요.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "문의 내용은 필수입니다.")
        String content,

        @Schema(description = "문의를 작성한 프론트 페이지 경로", example = "/user/problems/123")
        @Size(max = 500, message = "sourceUrl은 500자를 넘을 수 없습니다.")
        String sourceUrl
) {

    // 인증된 사용자 ID를 더해 command로 변환한다.
    public CreateInquiryCommand toCommand(Long userId) {
        return new CreateInquiryCommand(userId, content, sourceUrl);
    }
}
