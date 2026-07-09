package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.domain.repository.AuthSessionRepository;
import com.wanted.codebombalms.global.infrastructure.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthSessionManager {

    private final AuthSessionRepository authSessionRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 새 세션 오픈 — sid 발급 + Redis 저장(단일세션: 기존 세션 덮어씀).
     * 반환된 sid 를 AT 에 심는다. TTL = RT 만료(세션 최대 수명).
     */
    public String open(Long userId) {
        String sid = UUID.randomUUID().toString();
        authSessionRepository.save(userId, sid,
                Duration.ofMillis(jwtTokenProvider.getRefreshExpiration()));
        return sid;
    }

    /** 현재 세션 sid 반환 — 없으면 새로 오픈. 세션 유지형 AT 재발급용(예: 닉네임 변경). */
    public String currentSid(Long userId) {
        return authSessionRepository.findSid(userId)
                .orElseGet(() -> open(userId));
    }

    /** 세션 종료 — 잠금·탈퇴 시 호출 → 발급된 전 AT 즉시 무효 */
    public void close(Long userId) {
        authSessionRepository.delete(userId);
    }
}
