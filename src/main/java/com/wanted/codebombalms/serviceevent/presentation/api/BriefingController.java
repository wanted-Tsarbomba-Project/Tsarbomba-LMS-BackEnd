package com.wanted.codebombalms.serviceevent.presentation.api;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.serviceevent.application.query.BriefingResult;
import com.wanted.codebombalms.serviceevent.application.service.BriefingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 AI 브리핑 API — ADMIN·MASTER 전용. 권한 설계는 SecuritySummaryController 와 동일.
 */
@RestController
@RequestMapping("/api/v1/admin/security/briefing")
@PreAuthorize("hasAnyRole('ADMIN','MASTER')")
@RequiredArgsConstructor
public class BriefingController {

    private final BriefingService briefingService;

    @Operation(summary = "AI 브리핑 조회", description = "최신 저장본 즉시 반환 (LLM 호출 없음). 생성 이력 없으면 data=null")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "AUT-015: 권한 없음")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<BriefingResult>> getBriefing() {
        return ResponseEntity.ok(ApiResponse.success(
                "SECURITY-BRIEFING-RETRIEVED",
                "브리핑 조회 성공",
                briefingService.getLatest().orElse(null)
        ));
    }

    @Operation(summary = "AI 브리핑 재생성", description = "즉시 재생성 (동기 5~30초, 쿨다운 분당 1회)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재생성 성공 (새 브리핑 반환)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "SEC-001: 재생성 쿨다운"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "SEC-002: AI 생성 실패")
    })
    @PostMapping("/regenerate")
    public ResponseEntity<ApiResponse<BriefingResult>> regenerate() {
        return ResponseEntity.ok(ApiResponse.success(
                "SECURITY-BRIEFING-REGENERATED",
                "브리핑 재생성 성공",
                briefingService.regenerate()
        ));
    }
}
