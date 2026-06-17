package com.wanted.codebombalms.global.infrastructure.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(MdcLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long startAt = System.nanoTime();

        try {
            MDC.put("traceId", UUID.randomUUID().toString().substring(0, 8));
            MDC.put("requestURI", request.getRequestURI());
            MDC.put("method", request.getMethod());
            MDC.put("clientIp", resolveClientIp(request));

            log.info("event=request_started method={} uri={}",
                    request.getMethod(), request.getRequestURI());

            filterChain.doFilter(request, response);
        } finally {
            // MDC.clear() 전에 남겨야 완료 로그에도 traceId 가 붙는다.
            long durationMs = (System.nanoTime() - startAt) / 1_000_000;
            log.info("event=request_completed method={} uri={} status={} durationMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs);

            MDC.clear();
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
