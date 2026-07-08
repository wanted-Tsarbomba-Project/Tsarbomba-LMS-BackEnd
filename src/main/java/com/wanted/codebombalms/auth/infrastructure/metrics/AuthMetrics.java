package com.wanted.codebombalms.auth.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Auth 도메인 커스텀 메트릭 (관측 Layer 3).
 *
 * <p>Timer: 로그인 이력 조회 내부 구간 측정.
 * <p>Counter(KPI): {@code http_server_requests} 는 200이 "로그인 성공"인지 "step-up 필요"인지
 * 구분 못 한다. 비즈니스 outcome 차원({@code auth_login_total} / {@code auth_signup_total})을 채운다.
 */
@Component
public class AuthMetrics {

    private final Timer loginHistoryQueryTimer;

    // KPI 카운터 — outcome 태그로 분해. 시작 시 0으로 사전 등록(이벤트 없어도 패널에 0 표시).
    private final Counter loginSuccess;
    private final Counter loginStepUp;
    private final Counter loginFail;
    private final Counter signupSuccess;
    private final Counter signupFail;

    public AuthMetrics(MeterRegistry registry) {
        // 등록명엔 _seconds 를 붙이지 않는다. Timer 라서 Prometheus 가
        // auth_login_history_query_duration_seconds_{count,sum,max} 로 자동 변환한다.
        this.loginHistoryQueryTimer = Timer.builder("auth_login_history_query_duration")
                .description("로그인 이력 조회 구간 시간(login_history 페이지 쿼리)")
                .register(registry);

        // 등록명엔 _total 을 붙이지 않는다. Counter 라서 Prometheus 가
        // auth_login_total{outcome} / auth_signup_total{outcome} 로 변환한다.
        this.loginSuccess = loginCounter(registry, "success");
        this.loginStepUp  = loginCounter(registry, "stepup");
        this.loginFail    = loginCounter(registry, "fail");
        this.signupSuccess = signupCounter(registry, "success");
        this.signupFail    = signupCounter(registry, "fail");
    }

    private Counter loginCounter(MeterRegistry registry, String outcome) {
        return Counter.builder("auth_login")
                .tag("outcome", outcome)
                .description("로그인 시도 결과 (success/stepup/fail)")
                .register(registry);
    }

    private Counter signupCounter(MeterRegistry registry, String outcome) {
        return Counter.builder("auth_signup")
                .tag("outcome", outcome)
                .description("회원가입 결과 (success/fail)")
                .register(registry);
    }

    /** 로그인 이력 조회(login_history 페이지) 구간 소요 시간 기록. */
    public void recordLoginHistoryQuery(long elapsedNanos) {
        loginHistoryQueryTimer.record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    /** 정식 로그인 성공(신뢰기기 즉시 발급). */
    public void recordLoginSuccess() {
        loginSuccess.increment();
    }

    /** 추가 인증(step-up) 챌린지 발생 — 로그인 미완료. */
    public void recordLoginStepUp() {
        loginStepUp.increment();
    }

    /** 로그인 실패(자격 불일치/계정 잠금). */
    public void recordLoginFail() {
        loginFail.increment();
    }

    /** 회원가입 완료. */
    public void recordSignupSuccess() {
        signupSuccess.increment();
    }

    /** 회원가입 실패(검증/중복). */
    public void recordSignupFail() {
        signupFail.increment();
    }
}
