package com.wanted.codebombalms.auth.domain.repository;

import java.util.Optional;

/**
 * 비밀번호 재설정 코드 저장소 (Output Port).
 * reset 단계에서 email 없이 code 만으로 사용자를 찾기 위해
 * code 를 키로 email 을 역방향 저장한다. Redis TTL 자동 관리.
 */
public interface PasswordResetRepository {

    /**
     * 재설정 코드 저장 (SET NX). 이미 동일 코드가 존재하면 저장하지 않고 false 반환.
     * 6자리 코드 충돌 시 기존 code → email 매핑 덮어쓰기를 방지한다. (TTL 10분)
     */
    boolean saveCodeIfAbsent(String email, String code);

    /** 코드로 이메일 비파괴 조회 (verify-code 단계 — 코드 유지) */
    Optional<String> findEmailByCode(String code);

    /** 코드로 이메일 조회 + 즉시 삭제 (reset 단계 — 원자적 단일 사용 보장) */
    Optional<String> findAndDeleteByCode(String code);

    void markRecentlySent(String email);              // 재발송 쿨다운 (TTL 1분)

    boolean isRecentlySent(String email);

    long incrementSendCount(String email);            // 발송 횟수 1 증가 (TTL 10분)

    long getFailCount(String email);      // 재설정 시도 실패 횟수 조회 (verify-code / reset 무차별 대입 차단용)


    long incrementFailCount(String email);     // 재설정 시도 실패 횟수 1 증가 (TTL 10분)


    void clearFailCount(String email);    // 재설정 시도 실패 횟수 초기화 (성공 또는 새 코드 발급 시)

}
