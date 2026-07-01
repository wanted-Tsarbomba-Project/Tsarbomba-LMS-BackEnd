package com.wanted.codebombalms.auth.domain.repository;

import com.wanted.codebombalms.auth.domain.model.StepUpChallenge;

import java.util.Optional;

public interface StepUpTokenRepository {

    /** 챌린지 토큰으로 step-up 정보 저장 (TTL 5분). 재발송 시 덮어쓰기 */
    void save(String token, StepUpChallenge challenge);

    /** 검증/재발송 시 조회 (비파괴 — 성공 시 별도 delete) */
    Optional<StepUpChallenge> find(String token);

    /** 검증 성공/세션 종료 시 토큰 + 시도 카운터 삭제 */
    void delete(String token);

    /** OTP 오입력 시 시도 횟수 +1 → 누적값 반환 (토큰 TTL과 동기) */
    int incrementAttempts(String token);

    /**
     * 같은 기기(userId+deviceFp)의 step-up 발급을 원자적으로 1건으로 제한한다. (TTL 5분)
     * - 신규 발급권을 선점하면 Optional.empty() 반환 → 호출측이 코드 생성 + 메일 발송 진행
     * - 이미 유효한 발급이 있으면 그 토큰을 반환 → 호출측은 메일 재발송 없이 재사용
     */
    Optional<String> reserveDeviceChallenge(Long userId, String deviceFp, String newToken);
}
