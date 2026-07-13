package com.wanted.codebombalms.serviceevent.infrastructure.web;

import com.wanted.codebombalms.global.infrastructure.jwt.JwtAuthenticationFilter;
import com.wanted.codebombalms.global.infrastructure.logging.MdcLoggingFilter;
import com.wanted.codebombalms.global.infrastructure.metrics.HttpAnomalyReporter;
import com.wanted.codebombalms.global.infrastructure.web.ClientIpResolver;
import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventEnvelope;
import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventType;
import com.wanted.codebombalms.serviceevent.infrastructure.persistence.ServiceEventWriter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * 5xx 상세 기록 구현 — 예외 클래스명을 detail 로 적재, 중복 방지 마커로 MdcLoggingFilter 재적재 차단.
 * 기록 실패는 전부 흡수 — 에러 응답 비파괴 (best-effort).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServerErrorEventRecorder implements HttpAnomalyReporter {

    private final ServiceEventWriter serviceEventWriter;
    private final HttpAnomalyGuard anomalyGuard;

    @Override
    public void reportServerError(HttpServletRequest request, int httpStatus, Exception cause) {
        try {
            request.setAttribute(MdcLoggingFilter.ANOMALY_RECORDED_ATTRIBUTE, Boolean.TRUE);

            String route = HttpRequestAnomalySupport.normalizedRoute(request);
            if (!anomalyGuard.tryAcquire("5xx:" + route)) {
                return;
            }

            ServiceEventType type = httpStatus == 502
                    ? ServiceEventType.HTTP_502_EXTERNAL
                    : ServiceEventType.HTTP_5XX;
            Long userId = HttpRequestAnomalySupport.parseUserId(
                    request.getAttribute(JwtAuthenticationFilter.AUTHENTICATED_USER_ID_ATTRIBUTE));

            serviceEventWriter.write(ServiceEventEnvelope.httpAnomaly(
                    type, route, httpStatus, null,
                    ClientIpResolver.resolve(request), userId, MDC.get("traceId"),
                    "exception=" + cause.getClass().getSimpleName()));
        } catch (Exception ignored) {
            log.warn("event=server_error_record_failed reason={}", ignored.toString());
        }
    }
}
