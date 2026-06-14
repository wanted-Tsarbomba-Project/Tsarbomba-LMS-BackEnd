package com.wanted.codebombalms.admin.permission.presentation.api;

import com.wanted.codebombalms.admin.permission.domain.exception.AdminAuthErrorCode;
import com.wanted.codebombalms.global.presentation.api.common.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// master 전용 admin 권한 관리 API의 접근 거부 응답 코드를 도메인 컨벤션으로 맞춘다.
@RestControllerAdvice(assignableTypes = AdminPermissionController.class)
public class AdminPermissionExceptionHandler {

    // @PreAuthorize에서 MASTER가 아닌 사용자를 차단하면 ADM-AUTH-004로 응답한다.
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleMasterOnlyAccess(
            AuthorizationDeniedException e,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(403)
                .body(ApiErrorResponse.of(
                        403,
                        AdminAuthErrorCode.MASTER_ONLY_ACCESS_REQUIRED,
                        request.getRequestURI()
                ));
    }
}
