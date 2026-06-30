package com.wanted.codebombalms.auth.infrastructure.metrics;

import static com.wanted.codebombalms.auth.infrastructure.metrics.AuthSecurityEvent.*;

import com.wanted.codebombalms.global.infrastructure.metrics.SecurityEventReporter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * 비정상 행위를 Prometheus(집계) + Loki(상세) 이중 기록한다.
 *
 * <p>메트릭은 저카디널리티 집계용 {@code auth_security_event_total{category,type}} 하나로 통일.
 * ip/userId 같은 고카디널리티 정보는 메트릭 라벨이 아니라 로그 본문(logfmt)에만 담는다.
 */
@Slf4j
@Component
public class AuthSecurityEventRecorder implements SecurityEventReporter {

    // 등록명엔 _total 을 붙이지 않는다. Counter 라서 Prometheus 가 auth_security_event_total 로 변환한다.
    private static final String METRIC = "auth_security_event";

    // ErrorCode(AUT-*) → 이벤트. 흐름성/모호 코드(AUT-009 등)는 서비스에서 직접 기록한다.
    private static final Map<String, AuthSecurityEvent> BY_CODE = Map.ofEntries(
            Map.entry("AUT-001", LOGIN_FAIL),
            Map.entry("AUT-014", EMAIL_SEND_BLOCKED),
            Map.entry("AUT-017", PASSWORD_RESET_BLOCKED),
            Map.entry("AUT-019", OAUTH_TOKEN_EXCHANGE_FAIL),
            Map.entry("AUT-020", OAUTH_USERINFO_FAIL),
            Map.entry("AUT-021", OAUTH_STATE_INVALID),
            Map.entry("AUT-022", OAUTH_EMAIL_CONFLICT),
            Map.entry("AUT-023", OAUTH_EMAIL_NOT_VERIFIED),
            Map.entry("AUT-004", REFRESH_TOKEN_INVALID),
            Map.entry("AUT-005", REFRESH_TOKEN_INVALID),
            Map.entry("AUT-006", TEMP_TOKEN_INVALID),
            Map.entry("AUT-007", LOCK_TOKEN_INVALID),
            Map.entry("AUT-008", LOCK_TOKEN_INVALID),
            Map.entry("AUT-010", EMAIL_CODE_EXPIRED));

    private final MeterRegistry registry;

    public AuthSecurityEventRecorder(MeterRegistry registry) {
        this.registry = registry;
    }

    /** 흐름 기반 이벤트(의심 로그인 등) 직접 기록. ip/uri 는 MDC(MdcLoggingFilter)에서 가져온다. */
    public void record(AuthSecurityEvent event, Long userId) {
        registry.counter(METRIC, "category", event.getCategory(), "type", event.getType()).increment();
        log.warn("event=security_event category={} type={} userId={} ip={} uri={}",
                event.getCategory(), event.getType(),
                userId == null ? "-" : userId, mdc("clientIp"), mdc("requestURI"));
    }

    /** 예외 기반 이벤트 — GlobalExceptionHandler 가 ErrorCode 로 호출. 매핑 없는 코드는 무시. */
    @Override
    public void reportByErrorCode(String errorCode) {
        AuthSecurityEvent event = BY_CODE.get(errorCode);
        if (event != null) {
            record(event, null);
        }
    }

    private String mdc(String key) {
        String value = MDC.get(key);
        return value == null ? "-" : value;
    }
}
