package com.wanted.codebombalms.global.presentation.api.common;

import com.wanted.codebombalms.global.domain.common.error.DomainException;
import com.wanted.codebombalms.global.domain.common.error.exception.*;
import com.wanted.codebombalms.global.infrastructure.metrics.HttpAnomalyReporter;
import com.wanted.codebombalms.global.infrastructure.metrics.SecurityEventReporter;
import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
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
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Arrays;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Environment env;
    private final ObjectProvider<SecurityEventReporter> securityEventReporter;
    private final ObjectProvider<HttpAnomalyReporter> httpAnomalyReporter;

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainException(
            DomainException e, HttpServletRequest request) {
        log.warn("[{}] {} - path: {}", e.getHttpStatus(), e.getMessage(), request.getRequestURI());
        // 보안 이벤트 기록 실패는 삼킴 — 원래 에러 응답 보호
        try {
            securityEventReporter.ifAvailable(r -> r.reportByErrorCode(e.getErrorCode().getCode()));
        } catch (Exception ignored) {
            log.warn("보안 이벤트 기록 실패 (무시): {}", ignored.getMessage());
        }
        // 외부 서비스 예외(502)는 원인 클래스명 포함 상세 기록
        if (e instanceof ExternalServiceException) {
            reportServerErrorQuietly(request, e.getHttpStatus(), e);
        }
        return ResponseEntity.status(e.getHttpStatus())
                .body(ApiErrorResponse.of(e.getHttpStatus(), e.getErrorCode(), request.getRequestURI()));
    }
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class,
            HandlerMethodValidationException.class,
            HttpMessageNotReadableException.class,
            HttpMediaTypeNotSupportedException.class,
            MultipartException.class,
            MaxUploadSizeExceededException.class
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
        // unhandled 500 — 예외 클래스명 상세 기록은 HttpAnomalyReporter 구현 담당
        reportServerErrorQuietly(request, 500, e);
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

    /** 5xx 상세 기록 위임 — ifAvailable(빈 부재 시 무시) + 예외 삼킴 */
    private void reportServerErrorQuietly(HttpServletRequest request, int httpStatus, Exception cause) {
        try {
            httpAnomalyReporter.ifAvailable(r -> r.reportServerError(request, httpStatus, cause));
        } catch (Exception ignored) {
            log.warn("event=server_error_record_failed reason={}", ignored.toString());
        }
    }
}
