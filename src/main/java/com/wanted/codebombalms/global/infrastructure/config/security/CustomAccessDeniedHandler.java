package com.wanted.codebombalms.global.infrastructure.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.codebombalms.global.infrastructure.logging.SecurityEventLogger;
import com.wanted.codebombalms.global.presentation.api.common.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final SecurityEventLogger securityEventLogger;

    public CustomAccessDeniedHandler(
            ObjectMapper objectMapper,
            SecurityEventLogger securityEventLogger
    ) {
        this.objectMapper = objectMapper;
        this.securityEventLogger = securityEventLogger;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException e
    ) throws IOException {
        securityEventLogger.accessDenied(request);

        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        ApiErrorResponse.of(403, "AUT-015", "접근 권한이 없습니다.", request.getRequestURI())
                )
        );
    }
}
