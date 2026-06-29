package com.wanted.codebombalms.auth.domain.model;

/** 미신뢰 기기 로그인 시 추가 인증(step-up) 대기 정보 (Redis 임시토큰으로 조회) */
public record StepUpChallenge(
        Long userId,
        String deviceFp,
        String country,
        String code
) {
}
