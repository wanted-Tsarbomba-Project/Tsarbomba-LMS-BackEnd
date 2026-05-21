package com.wanted.codebombalms.auth.domain.repository;

import com.wanted.codebombalms.auth.domain.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {

    // 토큰 문자열로 조회
    Optional<RefreshToken> findByToken(String token);

    // 특정 유저의 모든 Refresh Token 삭제 (로그아웃 / 단일 세션 강제)
    void deleteByUserId(Long userId);

    // 저장
    RefreshToken save(RefreshToken refreshToken);
}