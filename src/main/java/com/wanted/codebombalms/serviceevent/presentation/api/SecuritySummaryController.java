package com.wanted.codebombalms.serviceevent.presentation.api;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.serviceevent.application.query.SecuritySummaryResult;
import com.wanted.codebombalms.serviceevent.application.service.SecuritySummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 보안 요약 콘솔 API (#608).
 *
 * <p>권한 설계: SecurityConfig 의 /api/v1/admin/** 는 OPERATOR 도 통과시키지만
 * 보안 데이터(IP·표적 계정)는 ADMIN·MASTER 전용 → @PreAuthorize 로 좁힌다.
 * AdminPermissionInterceptor 세밀권한(USER_MANAGEMENT 등)은 걸지 않는다 — 관리자
 * 공통 랜딩 화면이라 무권한 admin 도 조회는 가능해야 하고, 액션(계정 잠금)은
 * 기존 lock API 의 USER_MANAGEMENT 룰이 그대로 지킨다 (의도된 결정, #608).
 */
@RestController
@RequestMapping("/api/v1/admin/security")
@PreAuthorize("hasAnyRole('ADMIN','MASTER')")
@RequiredArgsConstructor
public class SecuritySummaryController {

    private final SecuritySummaryService securitySummaryService;

    @Operation(summary = "보안 요약 조회", description = "기간 내 이벤트 집계 — KPI·도메인 분포·HTTP 예외·위험 IP·시간대 분포")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "SEC-003: 지원하지 않는 기간 파라미터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "AUT-015: 권한 없음")
    })
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<SecuritySummaryResult>> getSummary(
            @RequestParam(defaultValue = "today") String period
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "SECURITY-SUMMARY-RETRIEVED",
                "보안 요약 조회 성공",
                securitySummaryService.getSummary(period)
        ));
    }
}
