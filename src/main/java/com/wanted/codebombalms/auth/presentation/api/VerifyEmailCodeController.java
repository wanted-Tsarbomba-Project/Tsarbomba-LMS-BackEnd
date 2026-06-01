package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.usecase.VerifyEmailCodeUseCase;
import com.wanted.codebombalms.auth.presentation.api.dto.request.VerifyEmailCodeRequest;
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

@Tag(name = "Auth - 이메일 인증", description = "회원가입 전 이메일 소유 확인 (Redis OTP) (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/auth/email")
@RequiredArgsConstructor
public class VerifyEmailCodeController {

    private final VerifyEmailCodeUseCase verifyEmailCodeUseCase;

    @Operation(
            summary = "이메일 인증 코드 검증",
            description = "6자리 OTP 검증. 성공 시 코드 즉시 삭제 + 인증 완료 플래그 Redis 30분 TTL 저장."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "AUT-009 유효하지 않은 코드 / AUT-010 만료된 코드")
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verify(
            @Valid @RequestBody VerifyEmailCodeRequest request
    ) {
        verifyEmailCodeUseCase.verifyCode(request.email(), request.code());

        return ResponseEntity.ok(ApiResponse.success(
                AuthResponseCode.EMAIL_VERIFIED,
                AuthResponseMessage.EMAIL_VERIFIED
        ));
    }
}