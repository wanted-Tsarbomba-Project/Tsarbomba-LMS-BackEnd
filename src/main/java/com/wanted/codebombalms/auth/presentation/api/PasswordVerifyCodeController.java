package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.usecase.VerifyPasswordResetCodeUseCase;
import com.wanted.codebombalms.auth.presentation.api.dto.request.PasswordVerifyCodeRequest;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth - 비밀번호 재설정", description = "비밀번호 재설정 코드 발송/검증/재설정 (Redis OTP) (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/auth/password")
@RequiredArgsConstructor
public class PasswordVerifyCodeController {

    private final VerifyPasswordResetCodeUseCase verifyPasswordResetCodeUseCase;

    @Operation(
            summary = "비밀번호 재설정 코드 검증",
            description = "6자리 재설정 코드 + 이메일 쌍의 유효성 검증. 코드는 소비하지 않고 reset 단계로 넘긴다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검증 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "AUT-011 유효하지 않은 코드 / AUT-012 만료된 코드")
    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<Void>> verifyCode(
            @Valid @RequestBody PasswordVerifyCodeRequest request
    ) {
        verifyPasswordResetCodeUseCase.verifyResetCode(request.email(), request.code());

        return ResponseEntity.ok(ApiResponse.success(
                AuthResponseCode.PASSWORD_RESET_CODE_VERIFIED,
                AuthResponseMessage.PASSWORD_RESET_CODE_VERIFIED
        ));
    }
}
