package com.wanted.codebombalms.user.domain.repository;

/**
 * 개인정보 수정 재인증(step-up) 플래그 저장소 (Output Port).
 * verify-password 성공 시 플래그를 심고, 수정 페이지 내 민감 작업(정보수정/비밀번호변경)에서 확인한다.
 * Redis 구현체에서 TTL 자동 관리.
 */
public interface ProfileEditVerificationRepository {

    /** 재인증 완료 플래그 저장 (TTL 3분 자동) */
    void markVerified(Long userId);

    /** 재인증 플래그 존재 여부 확인 — 삭제하지 않음(페이지 내 여러 작업 허용) */
    boolean isVerified(Long userId);

    /** 재인증 플래그 삭제 (비밀번호 변경 후 = 세션 종료 시) */
    void clearVerified(Long userId);
}
