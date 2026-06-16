package com.wanted.codebombalms.auth.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    // 로그인
    AUTH_LOGIN_FAIL("AUT-001", "이메일 또는 비밀번호가 일치하지 않습니다."),

    // 액세스 토큰
    AUTH_TOKEN_INVALID("AUT-002", "유효하지 않은 토큰입니다."),
    AUTH_TOKEN_EXPIRED("AUT-003", "만료된 토큰입니다."),

    // 리프레시 토큰
    AUTH_REFRESH_TOKEN_INVALID("AUT-004", "유효하지 않은 Refresh Token입니다."),
    AUTH_REFRESH_TOKEN_EXPIRED("AUT-005", "만료된 Refresh Token입니다."),

    // 임시 / 잠금 토큰
    AUTH_TEMP_TOKEN_INVALID("AUT-006", "유효하지 않은 임시 토큰입니다."),
    AUTH_LOCK_TOKEN_INVALID("AUT-007", "유효하지 않은 계정 잠금 토큰입니다."),
    AUTH_LOCK_TOKEN_EXPIRED("AUT-008", "만료된 계정 잠금 토큰입니다."),

    // 인증 코드
    AUTH_CODE_INVALID("AUT-009", "유효하지 않은 인증 코드입니다."),
    AUTH_CODE_EXPIRED("AUT-010", "만료된 인증 코드입니다."),

    // 비밀번호 재설정
    AUTH_PASSWORD_RESET_CODE_INVALID("AUT-011", "유효하지 않은 재설정 코드입니다."),
    AUTH_PASSWORD_RESET_CODE_EXPIRED("AUT-012", "만료된 재설정 코드입니다."),
    AUTH_PASSWORD_MISMATCH("AUT-013", "비밀번호가 일치하지 않습니다."),

    // 기타
    AUTH_EMAIL_SEND_TOO_MANY("AUT-014", "이메일 발송 횟수를 초과했습니다."),
    AUTH_FORBIDDEN("AUT-015", "접근 권한이 없습니다."),
    AUTH_REQUIRED("AUT-016", "인증이 필요합니다."),
    AUTH_PASSWORD_RESET_TOO_MANY("AUT-017", "비밀번호 재설정 시도 횟수를 초과했습니다. 잠시 후 다시 시도해주세요.");

    private final String code;
    private final String message;
}
