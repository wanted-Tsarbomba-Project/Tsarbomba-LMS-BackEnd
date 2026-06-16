package com.wanted.codebombalms.auth.domain.repository;

import java.util.Optional;

/**
 * 이메일 인증 코드 + 인증 완료 플래그 저장소 (Output Port).
 * Redis 구현체에서 TTL 자동 관리.
 */
public interface EmailVerificationRepository {

    // ===== 인증 코드 (TTL 3분) =====

    /** 인증 코드 저장 (TTL 3분 자동) */
    void saveCode(String email, String code);

    /** 인증 코드 조회 */
    Optional<String> findCode(String email);

    /** 인증 코드 삭제 (검증 성공 후 단일 사용 보장) */
    void deleteCode(String email);

    // ===== 인증 완료 플래그 (TTL 30분) =====

    /** 인증 완료 플래그 저장 (TTL 30분 자동) */
    void markVerified(String email);

    /** 인증 완료 여부 확인 */
    boolean isVerified(String email);

    /** 인증 완료 플래그 삭제 (회원가입 완료 후) */
    void clearVerified(String email);

    // ===== 재발송 쿨다운 =====

    /** 최근 발송 시점 기록 (TTL 1분) */
    void markRecentlySent(String email);

    /** 최근 1분 내 발송 여부 확인 */
    boolean isRecentlySent(String email);

    // ===== 발송 횟수 제한 =====

    /** 발송 횟수 1 증가 후 현재 카운트 반환 (TTL 10분) */
    long incrementSendCount(String email);
}
