package com.wanted.codebombalms.serviceevent.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

/**
 * HTTP 예외 신호 수집 공용 유틸 (#607 리뷰 반영).
 * MdcLoggingFilter 와 ServerErrorEventRecorder 가 같은 정규화 규칙을 쓰도록 단일화한다.
 */
public final class HttpRequestAnomalySupport {

    private HttpRequestAnomalySupport() {
    }

    /**
     * 정규화된 라우트 키 — raw URI 금지(경로변수·스캔 경로로 카디널리티 폭발).
     * DispatcherServlet 도달 전 차단된 요청(401 등)은 패턴이 없어 "unmatched".
     */
    public static String normalizedRoute(HttpServletRequest request) {
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return request.getMethod() + " " + (pattern == null ? "unmatched" : pattern.toString());
    }

    /** JwtAuthenticationFilter 가 심은 userId attribute 값을 Long 으로 변환 ("anonymous"·비숫자는 null) */
    public static Long parseUserId(Object attributeValue) {
        if (attributeValue == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(attributeValue));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
