package com.wanted.codebombalms.auth.domain.policy;

/**
 * 비밀번호 재설정 관련 정책 상수.
 * 여러 서비스에서 공유하여 정책 변경 시 단일 지점만 수정하도록 한다.
 */
public final class PasswordResetPolicy {

    private PasswordResetPolicy() {
    }

    /** email당 10분 내 최대 코드 검증 실패 허용 횟수 (초과 시 429) */
    public static final int MAX_FAIL_ATTEMPTS = 5;
}
