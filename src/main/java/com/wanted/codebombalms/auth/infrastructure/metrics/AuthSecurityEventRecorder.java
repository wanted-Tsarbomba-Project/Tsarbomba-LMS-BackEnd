package com.wanted.codebombalms.auth.infrastructure.metrics;

import static com.wanted.codebombalms.auth.infrastructure.metrics.AuthSecurityEvent.*;

import com.wanted.codebombalms.global.infrastructure.metrics.SecurityEventReporter;
import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventEnvelope;
import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventType;
import com.wanted.codebombalms.serviceevent.infrastructure.persistence.ServiceEventWriter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * 비정상 행위를 Prometheus(집계) + Loki(상세) + DB(service_event) 삼중 기록.
 * 고카디널리티 값(ip·userId)은 메트릭 라벨 금지 — 로그 본문·DB 행에만 기록 (DB 는 best-effort).
 */
@Slf4j
@Component
public class AuthSecurityEventRecorder implements SecurityEventReporter {

    // 등록명에 _total 미부여 — Counter 는 Prometheus 가 auth_security_event_total 로 자동 변환
    private static final String METRIC = "auth_security_event";

    // ErrorCode(AUT-*) → 이벤트 매핑. 흐름성·모호 코드(AUT-009 등)는 서비스에서 직접 기록
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

    // AuthSecurityEvent.type 문자열 → 전역 ServiceEventType (code 가 1:1 동일하다는 전제를 코드로 고정)
    private static final Map<String, ServiceEventType> SERVICE_EVENT_BY_TYPE =
            Arrays.stream(ServiceEventType.values())
                    .collect(Collectors.toUnmodifiableMap(ServiceEventType::code, type -> type));

    private final MeterRegistry registry;
    private final ServiceEventWriter serviceEventWriter;

    public AuthSecurityEventRecorder(MeterRegistry registry, ServiceEventWriter serviceEventWriter) {
        this.registry = registry;
        this.serviceEventWriter = serviceEventWriter;
    }

    /** 흐름 기반 이벤트(의심 로그인 등) 직접 기록. ip/uri 는 MDC(MdcLoggingFilter) 값 사용. */
    public void record(AuthSecurityEvent event, Long userId) {
        registry.counter(METRIC, "category", event.getCategory(), "type", event.getType()).increment();
        log.warn("event=security_event traceId={} category={} type={} userId={} clientIp={} uri={}",
                mdc("traceId"),
                event.getCategory(), event.getType(),
                userId == null ? "-" : userId, mdc("clientIp"), mdc("requestURI"));
        recordToServiceEvent(event, userId);
    }

    /** 예외 기반 이벤트 — GlobalExceptionHandler 가 ErrorCode 로 호출. 매핑 없는 코드는 무시. */
    @Override
    public void reportByErrorCode(String errorCode) {
        AuthSecurityEvent event = BY_CODE.get(errorCode);
        if (event != null) {
            record(event, null);
        }
    }

    /**
     * DB(service_event) 적재. 요청 스레드에서 MDC 값 캡처 필수 — @Async 스레드 미전파.
     * publishEvent(AFTER_COMMIT) 금지 — 로그인 실패(롤백) 흐름에서 유실.
     */
    private void recordToServiceEvent(AuthSecurityEvent event, Long userId) {
        try {
            ServiceEventType type = SERVICE_EVENT_BY_TYPE.get(event.getType());
            if (type == null) {
                log.warn("event=service_event_type_unmapped type={}", event.getType());
                return;
            }
            serviceEventWriter.write(ServiceEventEnvelope.security(
                    type, userId, MDC.get("clientIp"), MDC.get("requestURI"), MDC.get("traceId")));
        } catch (Exception e) {
            log.warn("event=service_event_record_failed type={} reason={}", event.getType(), e.toString());
        }
    }

    private String mdc(String key) {
        String value = MDC.get(key);
        return value == null ? "-" : value;
    }
}
