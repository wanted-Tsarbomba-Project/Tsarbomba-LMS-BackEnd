package com.wanted.codebombalms.global.infrastructure.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.codebombalms.global.presentation.api.common.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException e
    ) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        ApiErrorResponse.of(401, "AUT-002", "유효하지 않은 토큰입니다.", request.getRequestURI())
                )
        );
    }
}