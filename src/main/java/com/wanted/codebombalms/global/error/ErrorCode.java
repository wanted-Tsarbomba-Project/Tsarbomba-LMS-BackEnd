package com.wanted.codebombalms.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ===== AUTH =====
    AUTH_LOGIN_FAIL(HttpStatus.BAD_REQUEST, "이메일 또는 비밀번호가 일치하지 않습니다."),
    AUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    AUTH_REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다."),
    AUTH_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 Refresh Token입니다."),
    AUTH_TEMP_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 임시 토큰입니다."),
    AUTH_LOCK_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 계정 잠금 토큰입니다."),
    AUTH_LOCK_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "만료된 계정 잠금 토큰입니다."),
    AUTH_CODE_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 인증 코드입니다."),
    AUTH_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "만료된 인증 코드입니다."),
    AUTH_PASSWORD_RESET_CODE_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 재설정 코드입니다."),
    AUTH_PASSWORD_RESET_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "만료된 재설정 코드입니다."),
    AUTH_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    AUTH_EMAIL_SEND_TOO_MANY(HttpStatus.TOO_MANY_REQUESTS, "이메일 발송 횟수를 초과했습니다."),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // ===== USER =====
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    USER_EMAIL_DUPLICATED(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다."),
    USER_NICKNAME_DUPLICATED(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다."),
    USER_PASSWORD_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "비밀번호 형식이 올바르지 않습니다. (8자 이상, 영문+숫자+특수문자 조합)"),
    USER_PHONE_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "전화번호 형식이 올바르지 않습니다."),
    USER_PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호 확인이 일치하지 않습니다."),
    USER_ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "잠긴 계정입니다."),
    USER_SOCIAL_ACCOUNT_NO_PASSWORD(HttpStatus.BAD_REQUEST, "소셜 가입 계정은 비밀번호 재설정을 사용할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}