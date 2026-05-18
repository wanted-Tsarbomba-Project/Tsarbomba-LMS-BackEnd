package com.wanted.codebombalms.global.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.codebombalms.global.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    // CustomAccessDeniedHandler.java
    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {  // 클래스명과 일치해야 함
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException e
    ) throws IOException {
        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        new ErrorResponse(403, "A-015", "접근 권한이 없습니다.", request.getRequestURI())
                )
        );
    }
}