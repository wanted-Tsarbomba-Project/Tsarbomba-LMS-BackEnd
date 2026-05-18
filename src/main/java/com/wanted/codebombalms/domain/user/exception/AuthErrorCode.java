package com.wanted.codebombalms.domain.user.exception;

import com.wanted.codebombalms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    AUTH_LOGIN_FAIL(400, "A-001", "이메일 또는 비밀번호가 일치하지 않습니다."),
    AUTH_TOKEN_INVALID(401, "A-002", "유효하지 않은 토큰입니다."),
    AUTH_TOKEN_EXPIRED(401, "A-003", "만료된 토큰입니다."),
    AUTH_REFRESH_TOKEN_INVALID(401, "A-004", "유효하지 않은 Refresh Token입니다."),
    AUTH_REFRESH_TOKEN_EXPIRED(401, "A-005", "만료된 Refresh Token입니다."),
    AUTH_TEMP_TOKEN_INVALID(401, "A-006", "유효하지 않은 임시 토큰입니다."),
    AUTH_LOCK_TOKEN_INVALID(400, "A-007", "유효하지 않은 계정 잠금 토큰입니다."),
    AUTH_LOCK_TOKEN_EXPIRED(400, "A-008", "만료된 계정 잠금 토큰입니다."),
    AUTH_CODE_INVALID(400, "A-009", "유효하지 않은 인증 코드입니다."),
    AUTH_CODE_EXPIRED(400, "A-010", "만료된 인증 코드입니다."),
    AUTH_PASSWORD_RESET_CODE_INVALID(400, "A-011", "유효하지 않은 재설정 코드입니다."),
    AUTH_PASSWORD_RESET_CODE_EXPIRED(400, "A-012", "만료된 재설정 코드입니다."),
    AUTH_PASSWORD_MISMATCH(400, "A-013", "비밀번호가 일치하지 않습니다."),
    AUTH_EMAIL_SEND_TOO_MANY(429, "A-014", "이메일 발송 횟수를 초과했습니다."),
    AUTH_FORBIDDEN(403, "A-015", "접근 권한이 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}