package com.wanted.codebombalms.inquiry.presentation.api;

import com.wanted.codebombalms.auth.domain.exception.AuthErrorCode;
import com.wanted.codebombalms.global.domain.common.error.exception.UnauthorizedException;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.inquiry.application.usecase.GetActiveInquiryRepliesUseCase;
import com.wanted.codebombalms.inquiry.application.usecase.HideInquiryReplyUseCase;
import com.wanted.codebombalms.inquiry.presentation.api.response.ActiveInquiryReplyListResponse;
import com.wanted.codebombalms.inquiry.presentation.api.response.InquiryReplyVisibilityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Inquiry - 사용자 문의", description = "로그인 사용자의 문의 답변 조회/노출 상태 변경 API")
@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final GetActiveInquiryRepliesUseCase getActiveInquiryRepliesUseCase;
    private final HideInquiryReplyUseCase hideInquiryReplyUseCase;

    @Operation(summary = "내 미확인 문의 답변 조회", description = "로그인 첫 화면에서 모달로 노출할 답변 확인 전 문의 답변 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-016: 인증이 필요합니다.")
    })
    @GetMapping("/me/replies/active")
    public ResponseEntity<ApiResponse<ActiveInquiryReplyListResponse>> findActiveReplies(
            @AuthenticationPrincipal Long userId
    ) {
        validateAuthenticated(userId);

        var result = getActiveInquiryRepliesUseCase.getActiveReplies(userId);

        return ResponseEntity.ok(ApiResponse.success(
                InquiryResponseCode.ACTIVE_REPLIES_RETRIEVED,
                InquiryResponseMessage.ACTIVE_REPLIES_RETRIEVED,
                ActiveInquiryReplyListResponse.from(result)
        ));
    }

    @Operation(summary = "문의 답변 비노출 처리", description = "사용자가 답변 모달을 닫으면 해당 문의 답변을 로그인 첫 화면에서 다시 노출하지 않습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-016: 인증이 필요합니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "INQ-002: 문의를 찾을 수 없습니다.")
    })
    @PatchMapping("/{inquiryId}/reply-visibility")
    public ResponseEntity<ApiResponse<InquiryReplyVisibilityResponse>> hideReply(
            @PathVariable Long inquiryId,
            @AuthenticationPrincipal Long userId
    ) {
        validateAuthenticated(userId);

        var result = hideInquiryReplyUseCase.hideReply(inquiryId, userId);

        return ResponseEntity.ok(ApiResponse.success(
                InquiryResponseCode.REPLY_VISIBILITY_UPDATED,
                InquiryResponseMessage.REPLY_VISIBILITY_UPDATED,
                InquiryReplyVisibilityResponse.from(result)
        ));
    }

    private void validateAuthenticated(Long userId) {
        if (userId == null) {
            throw new UnauthorizedException(AuthErrorCode.AUTH_REQUIRED);
        }
    }
}
