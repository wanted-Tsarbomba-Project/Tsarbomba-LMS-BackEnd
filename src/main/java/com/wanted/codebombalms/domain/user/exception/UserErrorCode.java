package com.wanted.codebombalms.domain.user.exception;

import com.wanted.codebombalms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(404, "U-001", "존재하지 않는 회원입니다."),
    USER_EMAIL_DUPLICATED(400, "U-002", "이미 사용 중인 이메일입니다."),
    USER_NICKNAME_DUPLICATED(400, "U-003", "이미 사용 중인 닉네임입니다."),
    USER_PASSWORD_FORMAT_INVALID(400, "U-004", "비밀번호 형식이 올바르지 않습니다. (8자 이상, 영문+숫자+특수문자 조합)"),
    USER_PHONE_FORMAT_INVALID(400, "U-005", "전화번호 형식이 올바르지 않습니다."),
    USER_PASSWORD_CONFIRM_MISMATCH(400, "U-006", "비밀번호 확인이 일치하지 않습니다."),
    USER_ACCOUNT_LOCKED(403, "U-007", "잠긴 계정입니다."),
    USER_SOCIAL_ACCOUNT_NO_PASSWORD(400, "U-008", "소셜 가입 계정은 비밀번호 재설정을 사용할 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}