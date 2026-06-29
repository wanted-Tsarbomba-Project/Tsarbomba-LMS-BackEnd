package com.wanted.codebombalms.auth.domain.repository;

import java.util.Optional;

public interface LockTokenRepository {

    /** 계정 잠금 토큰 저장 (token → userId, TTL 24시간) */
    void save(String token, Long userId);

    /** 잠금 링크 클릭 시 — 조회+삭제 단일 원자 연산(GETDEL, 단일 사용) → userId */
    Optional<Long> findUserIdAndDelete(String token);
}
