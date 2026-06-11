package com.wanted.codebombalms.admin.permission.domain.exception;

import com.wanted.codebombalms.global.domain.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminAuthErrorCode implements ErrorCode {

    ADMIN_PERMISSION_REQUIRED(
            "ADM-AUTH-001",
            "관리자 권한이 없습니다."
    ),
    USER_MANAGEMENT_PERMISSION_REQUIRED(
            "ADM-AUTH-002",
            "유저 관리 권한이 없습니다. MASTER 관리자에게 권한 부여를 요청해주세요."
    ),
    RULE_MANAGEMENT_PERMISSION_REQUIRED(
            "ADM-AUTH-003",
            "운영 규칙 관리 권한이 없습니다. MASTER 관리자에게 권한 부여를 요청해주세요."
    );

    private final String code;
    private final String message;
}
