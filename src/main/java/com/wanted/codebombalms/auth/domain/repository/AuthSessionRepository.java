package com.wanted.codebombalms.auth.domain.repository;

import java.time.Duration;
import java.util.Optional;

public interface AuthSessionRepository {

    /** 로그인 시 단일 세션 저장 (userId → sid). 기존 값 덮어씀 = 이전 세션(옛 AT) 축출. */
    void save(Long userId, String sid, Duration ttl);

    /** 현재 유효 세션 sid 조회 — 필터가 AT 의 sid 와 대조. */
    Optional<String> findSid(Long userId);

    /** 세션 무효화 — 잠금·탈퇴 시 호출 → 발급된 전 AT 즉시 무효. */
    void delete(Long userId);
}
