package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.usecase.SignupUseCase;
import com.wanted.codebombalms.auth.presentation.api.dto.request.SignupRequest;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Tag(name = "Auth - 인증", description = "회원가입 / 로그인 / 토큰 관리 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class SignupController {

    private final SignupUseCase signupUseCase;

    @Operation(
            summary = "회원가입",
            description = "이메일/비밀번호로 신규 회원가입. 사전에 이메일 인증 완료 필수."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "USR-009 이메일 미인증 / USR-006 비밀번호 확인 불일치 / 형식 오류")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "USR-002 이메일 중복 / USR-003 닉네임 중복")

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Long>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        Long userId = signupUseCase.signup(request.toCommand());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        AuthResponseCode.SIGNUP_COMPLETED,
                        AuthResponseMessage.SIGNUP_COMPLETED,
                        userId
                ));
    }
}
