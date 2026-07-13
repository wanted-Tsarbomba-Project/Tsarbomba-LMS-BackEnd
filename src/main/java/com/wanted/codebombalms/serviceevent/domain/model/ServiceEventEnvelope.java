package com.wanted.codebombalms.serviceevent.domain.model;

import java.time.LocalDateTime;

/**
 * 이벤트 발생 시점의 컨텍스트를 값으로 캡처하는 불변 봉투.
 * 비동기(@Async) 경계를 넘으므로 스레드 로컬 미의존 — 생성 시점에 모든 값 고정.
 */
public record ServiceEventEnvelope(
        ServiceEventType type,
        Long userId,
        Long targetId,
        String ipAddress,
        String uri,
        Integer httpStatus,
        Integer durationMs,
        String traceId,
        String detail,
        LocalDateTime occurredAt
) {

    private static final int MAX_URI = 255;
    private static final int MAX_DETAIL = 500;
    private static final int MAX_TRACE_ID = 8;

    public ServiceEventEnvelope {
        if (type == null) {
            throw new IllegalArgumentException("ServiceEventType must not be null");
        }
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now(); // occurredAt null → now() 보정
        }
        uri = truncate(uri, MAX_URI);
        detail = truncate(detail, MAX_DETAIL);
        traceId = truncate(traceId, MAX_TRACE_ID);
    }

    /** 비즈니스 이벤트용 */
    public static ServiceEventEnvelope business(ServiceEventType type, Long userId, Long targetId) {
        return business(type, userId, targetId, null);
    }

    public static ServiceEventEnvelope business(
            ServiceEventType type, Long userId, Long targetId, String detail) {
        return new ServiceEventEnvelope(
                type, userId, targetId, null, null, null, null, null, detail, LocalDateTime.now());
    }

    /** 보안 이벤트용 — 호출측이 MDC 값을 캡처해 전달 */
    public static ServiceEventEnvelope security(
            ServiceEventType type, Long userId, String ipAddress, String uri, String traceId) {
        return new ServiceEventEnvelope(
                type, userId, null, ipAddress, uri, null, null, traceId, null, LocalDateTime.now());
    }

    /** HTTP 예외 신호용 (필터/예외 핸들러) */
    public static ServiceEventEnvelope httpAnomaly(
            ServiceEventType type, String uri, Integer httpStatus, Integer durationMs,
            String ipAddress, Long userId, String traceId, String detail) {
        return new ServiceEventEnvelope(
                type, userId, null, ipAddress, uri, httpStatus, durationMs, traceId, detail,
                LocalDateTime.now());
    }

    /** 운영 지표 스냅샷 — 카운트를 targetId에 저장 */
    public static ServiceEventEnvelope opsMetric(ServiceEventType type, long count) {
        return new ServiceEventEnvelope(
                type, null, count, null, null, null, null, null, null, LocalDateTime.now());
    }

    public ServiceEventCategory category() {
        return type.category();
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
