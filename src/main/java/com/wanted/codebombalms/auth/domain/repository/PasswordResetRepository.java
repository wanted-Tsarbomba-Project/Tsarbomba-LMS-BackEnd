package com.wanted.codebombalms.auth.domain.repository;

import java.util.Optional;

/**
 * 비밀번호 재설정 코드 저장소 (Output Port).
 * reset 단계에서 email 없이 code 만으로 사용자를 찾기 위해
 * code 를 키로 email 을 역방향 저장한다. Redis TTL 자동 관리.
 */
public interface PasswordResetRepository {

    void saveCode(String email, String code);        // password:reset:{code} = email (TTL 10분)

    Optional<String> findEmailByCode(String code);   // 코드로 이메일 역조회

    void deleteByCode(String code);                   // 재설정 완료 후 단일 사용 보장

    void markRecentlySent(String email);              // 재발송 쿨다운 (TTL 1분)

    boolean isRecentlySent(String email);

    long incrementSendCount(String email);            // 발송 횟수 1 증가 (TTL 10분)
}
