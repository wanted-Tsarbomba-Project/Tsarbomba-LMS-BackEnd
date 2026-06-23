package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.usecase.SendPasswordResetCodeUseCase;
import com.wanted.codebombalms.auth.presentation.api.dto.request.PasswordForgotRequest;
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
public class PasswordForgotController {

    private final SendPasswordResetCodeUseCase sendPasswordResetCodeUseCase;

    @Operation(
            summary = "비밀번호 재설정 코드 요청",
            description = "가입 이메일로 6자리 재설정 코드 발송. code → email 역방향으로 Redis 10분 TTL 저장."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "발송 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USR-001 존재하지 않는 이메일")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "USR-008 소셜 가입 계정")
    @PostMapping("/forgot")
    public ResponseEntity<ApiResponse<Void>> forgot(
            @Valid @RequestBody PasswordForgotRequest request
    ) {
        sendPasswordResetCodeUseCase.sendResetCode(request.email());

        return ResponseEntity.ok(ApiResponse.success(
                AuthResponseCode.PASSWORD_RESET_CODE_SENT,
                AuthResponseMessage.PASSWORD_RESET_CODE_SENT
        ));
    }
}
