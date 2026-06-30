package com.wanted.codebombalms.global.infrastructure.logging;

import com.wanted.codebombalms.global.infrastructure.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventLogger {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventLogger.class);
    private static final String ANONYMOUS = "anonymous";
    private static final String ADMIN_API_PREFIX = "/api/v1/admin/";

    public void accessDenied(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String type = resolveAccessDeniedType(uri);

        log.warn("event=security_event type={} method={} uri={} status=403 userId={} role={} clientIp={}",
                type,
                request.getMethod(),
                uri,
                resolveAttribute(request, JwtAuthenticationFilter.AUTHENTICATED_USER_ID_ATTRIBUTE),
                resolveAttribute(request, JwtAuthenticationFilter.AUTHENTICATED_ROLE_ATTRIBUTE),
                resolveClientIp(request));
    }

    private String resolveAccessDeniedType(String uri) {
        return uri != null && uri.startsWith(ADMIN_API_PREFIX)
                ? "ADMIN_API_ACCESS_DENIED"
                : "ACCESS_DENIED";
    }

    private String resolveAttribute(HttpServletRequest request, String name) {
        Object value = request.getAttribute(name);
        return value == null ? ANONYMOUS : String.valueOf(value);
    }

    private String resolveClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
