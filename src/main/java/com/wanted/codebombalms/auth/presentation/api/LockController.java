package com.wanted.codebombalms.auth.presentation.api;

import com.wanted.codebombalms.auth.application.usecase.LockAccountUseCase;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth - 인증", description = "회원가입 / 로그인 / 토큰 관리 (담당: 김동현)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class LockController {

    private final LockAccountUseCase lockAccountUseCase;

    @Operation(summary = "계정 잠금", description = "의심 로그인 알림 이메일의 링크 클릭 시 계정을 잠그고 모든 세션을 종료한다. (비회원 접근 — 토큰으로 식별)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "잠금 처리 성공")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "AUT-007 유효하지 않은(또는 만료된) 잠금 토큰")
    @GetMapping("/lock")
    public ResponseEntity<ApiResponse<Void>> lock(@RequestParam String token) {
        lockAccountUseCase.lock(token);

        return ResponseEntity.ok(ApiResponse.<Void>success(
                AuthResponseCode.ACCOUNT_LOCKED,
                AuthResponseMessage.ACCOUNT_LOCKED,
                null
        ));
    }
}
