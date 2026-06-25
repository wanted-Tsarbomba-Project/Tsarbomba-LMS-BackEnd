package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.usecase.StepUpResendUseCase;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth - 인증", description = "회원가입 / 로그인 / 토큰 관리 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class StepUpResendController {

    private final StepUpResendUseCase stepUpResendUseCase;

    @Operation(summary = "추가 인증 코드 재발송", description = "stepupToken 으로 새 OTP 를 생성해 이메일로 재발송한다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재발송 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "AUT-024 step-up 임시토큰 무효/만료")
    @PostMapping("/step-up/resend")
    public ResponseEntity<ApiResponse<Void>> resend(
            @CookieValue(name = "stepupToken", required = false) String stepUpToken
    ) {
        stepUpResendUseCase.resend(stepUpToken);

        return ResponseEntity.ok(ApiResponse.<Void>success(
                AuthResponseCode.STEP_UP_CODE_RESENT,
                AuthResponseMessage.STEP_UP_CODE_RESENT,
                null
        ));
    }
}
