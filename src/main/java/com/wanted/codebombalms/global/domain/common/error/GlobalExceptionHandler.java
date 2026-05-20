package com.wanted.codebombalms.global.domain.common.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Environment env;

    // ===== 비즈니스 예외 (우리가 만든 예외 전부) =====
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException e,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = e.getErrorCode();

        if (isDev()) {
            log.warn("[GlobalExceptionHandler] {} - path: {}", e.getMessage(), request.getRequestURI(), e);
        } else {
            log.warn("[GlobalExceptionHandler] {} - path: {}", e.getMessage(), request.getRequestURI());
        }

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode, request.getRequestURI()));
    }

    // ===== @Valid 검증 실패 =====
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();

        log.warn("[GlobalExceptionHandler] validation failed - path: {}, message: {}",
                request.getRequestURI(), message);

        return ResponseEntity
                .status(400)
                .body(new ErrorResponse(400, "VALIDATION_FAILED", message, request.getRequestURI()));
    }

    // ===== 예상치 못한 예외 (항상 스택트레이스) =====
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e,
            HttpServletRequest request
    ) {
        log.error("[GlobalExceptionHandler] 예상치 못한 예외 - path: {}", request.getRequestURI(), e);

        String message = isDev() ? e.getMessage() : "서버 오류가 발생했습니다.";

        return ResponseEntity
                .status(500)
                .body(new ErrorResponse(500, "INTERNAL_ERROR", message, request.getRequestURI()));
    }

    private boolean isDev() {
        return Arrays.asList(env.getActiveProfiles()).contains("local");
    }



    // ===== 메서드 레벨 보안 거부 (@PreAuthorize 등) =====
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(
            AuthorizationDeniedException e,
            HttpServletRequest request
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);

        if (!isAuthenticated) {
            log.warn("[GlobalExceptionHandler] 인증 필요 - path: {}, reason: {}",
                    request.getRequestURI(), e.getMessage());
            return ResponseEntity
                    .status(401)
                    .body(new ErrorResponse(401, "AUTH_REQUIRED", "인증이 필요합니다.", request.getRequestURI()));
        }
        log.warn("[GlobalExceptionHandler] 권한 없음 - path: {}, reason: {}",
                request.getRequestURI(), e.getMessage());
        return ResponseEntity
                .status(403)
                .body(new ErrorResponse(403, "AUTH_FORBIDDEN", "권한이 없습니다.", request.getRequestURI()));
    }
}