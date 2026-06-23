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
    ),
    MASTER_ONLY_ACCESS_REQUIRED(
            "ADM-AUTH-004",
            "최고 관리자만 접근할 수 있습니다."
    ),
    INVALID_ADMIN_PERMISSION_REQUEST(
            "ADM-AUTH-005",
            "관리자 권한 수정 요청이 올바르지 않습니다."
    ),
    ADMIN_ACCOUNT_NOT_FOUND(
            "ADM-AUTH-006",
            "관리자 계정을 찾을 수 없습니다."
    ),
    INVALID_ADMIN_PERMISSION_STATE(
            "ADM-AUTH-007",
            "관리자 권한 상태가 올바르지 않습니다."
    );

    private final String code;
    private final String message;
}
