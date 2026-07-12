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
 * 5xx 상세 기록 구현 (#606, #607 리뷰로 GlobalExceptionHandler 에서 이관).
 * 예외 클래스명을 detail 로 남기고, 중복 방지 마커를 세워 MdcLoggingFilter 의
 * 상태코드 기반 기록이 같은 요청을 다시 적재하지 않게 한다.
 * 기록 실패는 전부 삼킨다 — 에러 응답을 절대 깨지 않는다 (best-effort).
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
