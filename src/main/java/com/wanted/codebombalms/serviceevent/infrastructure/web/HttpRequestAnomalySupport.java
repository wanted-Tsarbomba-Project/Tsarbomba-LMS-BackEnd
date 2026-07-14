package com.wanted.codebombalms.serviceevent.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

/**
 * HTTP 예외 신호 수집 공용 유틸.
 * 사용처: MdcLoggingFilter·ServerErrorEventRecorder — 동일 정규화 규칙 공유.
 */
public final class HttpRequestAnomalySupport {

    private HttpRequestAnomalySupport() {
    }

    /** 정규화 라우트 키 — raw URI 금지(카디널리티 폭발 방지). 매칭 패턴 없으면 "unmatched" */
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
