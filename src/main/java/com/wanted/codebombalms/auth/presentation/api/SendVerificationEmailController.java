package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.usecase.SendVerificationEmailUseCase;
import com.wanted.codebombalms.auth.presentation.api.dto.request.SendVerificationEmailRequest;
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
public class SendVerificationEmailController {

    private final SendVerificationEmailUseCase sendVerificationEmailUseCase;

    @Operation(
            summary = "이메일 인증 코드 발송",
            description = "6자리 OTP를 이메일로 발송. Redis TTL 3분 / 재발송 쿨다운 1분 / 10분 내 최대 5회 제한."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "발송 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "USR-002 이미 가입된 이메일")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "AUT-014 발송 횟수 초과 / 쿨다운")
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> send(
            @Valid @RequestBody SendVerificationEmailRequest request
    ) {
        sendVerificationEmailUseCase.sendVerificationCode(request.email());

        return ResponseEntity.ok(ApiResponse.success(
                AuthResponseCode.EMAIL_VERIFICATION_SENT,
                AuthResponseMessage.EMAIL_VERIFICATION_SENT
        ));
    }
}
