package com.wanted.codebombalms.global.presentation.api.common;

import com.wanted.codebombalms.global.domain.common.error.DomainException;
import com.wanted.codebombalms.global.domain.common.error.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Arrays;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Environment env;

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainException(
            DomainException e, HttpServletRequest request) {
        log.warn("[{}] {} - path: {}", e.getHttpStatus(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(e.getHttpStatus())
                .body(ApiErrorResponse.of(e.getHttpStatus(), e.getErrorCode(), request.getRequestURI()));
    }
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            HttpMediaTypeNotSupportedException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequestException(
            Exception e,
            HttpServletRequest request
    ) {
        log.warn("[400] 잘못된 요청 - path: {}, message: {}",
                request.getRequestURI(),
                e.getMessage()
        );

        String message = isDev()
                ? e.getMessage()
                : "요청 형식이 올바르지 않습니다.";

        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(
                        400,
                        "COMMON-BAD-REQUEST",
                        message,
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(
            Exception e, HttpServletRequest request) {
        log.error("[500] 예상치 못한 예외 - path: {}", request.getRequestURI(), e);
        String message = isDev() ? e.getMessage() : "서버 오류가 발생했습니다.";
        return ResponseEntity.status(500)
                .body(ApiErrorResponse.of(500, "INTERNAL_ERROR", message, request.getRequestURI()));
    }

    private boolean isDev() {
        return Arrays.asList(env.getActiveProfiles()).contains("local");
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthorizationDenied(
            AuthorizationDeniedException e, HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);

        if (!isAuthenticated) {
            log.warn("[401] 인증되지 않은 요청 - path: {}", request.getRequestURI());
            return ResponseEntity.status(401)
                    .body(ApiErrorResponse.of(401, "AUT-016", "인증이 필요합니다.", request.getRequestURI()));
        } else {
            log.warn("[403] 권한 부족 - path: {}", request.getRequestURI());
            return ResponseEntity.status(403)
                    .body(ApiErrorResponse.of(403, "AUT-015", "접근 권한이 없습니다.", request.getRequestURI()));
        }
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException e, HttpServletRequest request) {
        log.warn("[409] 데이터 무결성 위반(unique 충돌 추정) - path: {}, message: {}",
                request.getRequestURI(), e.getMostSpecificCause().getMessage());
        return ResponseEntity.status(409)
                .body(ApiErrorResponse.of(
                        409,
                        "COMMON-CONFLICT",
                        "이미 사용 중인 값입니다.",
                        request.getRequestURI()
                ));
    }
}

