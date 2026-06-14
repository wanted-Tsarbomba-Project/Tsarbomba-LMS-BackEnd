package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.usecase.ResetPasswordUseCase;
import com.wanted.codebombalms.auth.presentation.api.dto.request.PasswordResetRequest;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth - 비밀번호 재설정", description = "비밀번호 재설정 코드 발송/검증/재설정 (Redis OTP) (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/auth/password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final ResetPasswordUseCase resetPasswordUseCase;

    @Operation(
            summary = "비밀번호 재설정",
            description = "6자리 코드로 사용자 식별 후 새 비밀번호로 교체. 처리 후 코드 삭제 + RT 전체 삭제(강제 재로그인)."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재설정 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "AUT-011 유효하지 않은 코드 / AUT-012 만료된 코드 / USR-004 비밀번호 형식 오류")
    @PutMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> reset(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        resetPasswordUseCase.resetPassword(request.code(), request.newPassword());

        return ResponseEntity.ok(ApiResponse.success(
                AuthResponseCode.PASSWORD_RESET_COMPLETED,
                AuthResponseMessage.PASSWORD_RESET_COMPLETED
        ));
    }
}
