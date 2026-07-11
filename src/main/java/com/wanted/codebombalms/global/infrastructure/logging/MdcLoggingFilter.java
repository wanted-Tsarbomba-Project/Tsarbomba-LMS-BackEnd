package com.wanted.codebombalms.global.infrastructure.logging;

import com.wanted.codebombalms.global.infrastructure.jwt.JwtAuthenticationFilter;
import com.wanted.codebombalms.global.infrastructure.web.ClientIpResolver;
import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventEnvelope;
import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventType;
import com.wanted.codebombalms.serviceevent.infrastructure.persistence.ServiceEventWriter;
import com.wanted.codebombalms.serviceevent.infrastructure.web.HttpAnomalyGuard;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.UUID;

public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(MdcLoggingFilter.class);
    private static final String ANONYMOUS = "anonymous";

    /**
     * GlobalExceptionHandler 가 이미 상세(예외 클래스명 포함)를 기록한 요청에 세팅하는 마커.
     * 필터의 상태코드 기반 기록과 중복 적재를 막는다 (#606).
     */
    public static final String ANOMALY_RECORDED_ATTRIBUTE = "serviceEvent.anomalyRecorded";

    private final ServiceEventWriter serviceEventWriter;
    private final HttpAnomalyGuard anomalyGuard;
    private final long slowThresholdMs;

    public MdcLoggingFilter(
            ServiceEventWriter serviceEventWriter,
            HttpAnomalyGuard anomalyGuard,
            long slowThresholdMs) {
        this.serviceEventWriter = serviceEventWriter;
        this.anomalyGuard = anomalyGuard;
        this.slowThresholdMs = slowThresholdMs;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        long startAt = System.nanoTime();
        // traceId 는 로컬 변수로 잡아둔다 — doFilter 내부 필터가 MDC.clear() 해도 완료 로그에 안전.
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        try {
            MDC.put("traceId", traceId);
            MDC.put("requestURI", request.getRequestURI());
            MDC.put("method", request.getMethod());
            MDC.put("clientIp", ClientIpResolver.resolve(request));

            log.info("event=request_started method={} uri={}",
                    request.getMethod(), request.getRequestURI());

            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startAt) / 1_000_000;
            String userId = resolveAttribute(request, JwtAuthenticationFilter.AUTHENTICATED_USER_ID_ATTRIBUTE);
            String role = resolveAttribute(request, JwtAuthenticationFilter.AUTHENTICATED_ROLE_ATTRIBUTE);
            MDC.put("userId", userId);
            MDC.put("role", role);

            log.info("event=request_completed traceId={} method={} uri={} status={} durationMs={} userId={} role={} clientIp={}",
                    traceId,
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs,
                    userId,
                    role,
                    ClientIpResolver.resolve(request));

            recordAnomalyIfNeeded(request, response, durationMs, userId, traceId);

            MDC.clear();
        }
    }

    /**
     * 예외 신호만 조건부 적재 (#606). 정상 요청(2xx·3xx·일반 4xx)은 절대 적재하지 않는다.
     * 어떤 예외도 응답에 영향을 주지 않는다 (best-effort).
     */
    private void recordAnomalyIfNeeded(
            HttpServletRequest request, HttpServletResponse response,
            long durationMs, String userIdAttribute, String traceId) {
        try {
            int status = response.getStatus();
            String route = normalizedRoute(request);
            Long userId = parseUserId(userIdAttribute);
            String clientIp = ClientIpResolver.resolve(request);

            if (durationMs >= slowThresholdMs && anomalyGuard.tryAcquire("slow:" + route)) {
                serviceEventWriter.write(ServiceEventEnvelope.httpAnomaly(
                        ServiceEventType.SLOW_REQUEST, route, status, (int) durationMs,
                        clientIp, userId, traceId, null));
            }

            // 핸들러가 이미 상세 기록한 요청은 상태코드 기반 기록 생략 (중복 방지)
            if (request.getAttribute(ANOMALY_RECORDED_ATTRIBUTE) != null) {
                return;
            }

            if (status >= 500 && anomalyGuard.tryAcquire("5xx:" + route)) {
                serviceEventWriter.write(ServiceEventEnvelope.httpAnomaly(
                        ServiceEventType.HTTP_5XX, route, status, (int) durationMs,
                        clientIp, userId, traceId, null));
            } else if (status == 401) {
                boolean allowed = (userId == null)
                        ? anomalyGuard.tryAcquireAnonymous401()
                        : anomalyGuard.tryAcquire("401:" + route);
                if (allowed) {
                    serviceEventWriter.write(ServiceEventEnvelope.httpAnomaly(
                            ServiceEventType.AUTH_401_SPIKE, route, status, (int) durationMs,
                            clientIp, userId, traceId, null));
                }
            } else if (status == 403 && anomalyGuard.tryAcquire("403:" + route)) {
                serviceEventWriter.write(ServiceEventEnvelope.httpAnomaly(
                        ServiceEventType.ACCESS_403, route, status, (int) durationMs,
                        clientIp, userId, traceId, null));
            }
        } catch (Exception e) {
            log.warn("event=http_anomaly_record_failed reason={}", e.toString());
        }
    }

    /**
     * 정규화된 라우트 키 — raw URI 금지(경로변수·스캔 경로로 카디널리티 폭발).
     * DispatcherServlet 도달 전 차단된 요청(401 등)은 패턴이 없어 "unmatched".
     */
    private String normalizedRoute(HttpServletRequest request) {
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return request.getMethod() + " " + (pattern == null ? "unmatched" : pattern.toString());
    }

    private Long parseUserId(String attribute) {
        if (attribute == null || ANONYMOUS.equals(attribute)) {
            return null;
        }
        try {
            return Long.parseLong(attribute);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String resolveAttribute(HttpServletRequest request, String name) {
        Object value = request.getAttribute(name);
        return value == null ? ANONYMOUS : String.valueOf(value);
    }
}
