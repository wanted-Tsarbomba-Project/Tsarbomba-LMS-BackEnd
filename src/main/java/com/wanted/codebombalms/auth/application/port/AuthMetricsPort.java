package com.wanted.codebombalms.auth.application.port;

/**
 * Auth 도메인 KPI 계측 포트 — application 이 infrastructure(AuthMetrics)에
 * 직접 의존하지 않도록 의존 방향을 역전한다(헥사고날). AuthMetrics 가 구현한다.
 */
public interface AuthMetricsPort {

    /** 정식 로그인 성공(신뢰기기 즉시 발급). */
    void recordLoginSuccess();

    /** 추가 인증(step-up) 챌린지 발생 — 로그인 미완료. */
    void recordLoginStepUp();

    /** 로그인 실패(자격 불일치/계정 잠금). */
    void recordLoginFail();

    /** 회원가입 완료. */
    void recordSignupSuccess();

    /** 회원가입 실패(검증/중복/DB 제약). */
    void recordSignupFail();
}
