package com.wanted.codebombalms.inquiry.presentation.api;

import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import com.wanted.codebombalms.inquiry.application.query.GetAdminInquiriesQuery;
import com.wanted.codebombalms.inquiry.application.usecase.GetAdminInquiriesUseCase;
import com.wanted.codebombalms.inquiry.application.usecase.GetAdminInquiryDetailUseCase;
import com.wanted.codebombalms.inquiry.application.usecase.ReplyInquiryUseCase;
import com.wanted.codebombalms.inquiry.application.usecase.UpdateInquiryClassificationUseCase;
import com.wanted.codebombalms.inquiry.application.usecase.UpdateInquiryFilterUseCase;
import com.wanted.codebombalms.inquiry.domain.model.InquiryDomain;
import com.wanted.codebombalms.inquiry.domain.model.InquirySeverity;
import com.wanted.codebombalms.inquiry.domain.model.InquiryStatus;
import com.wanted.codebombalms.inquiry.presentation.api.request.InquiryClassificationUpdateRequest;
import com.wanted.codebombalms.inquiry.presentation.api.request.InquiryFilterUpdateRequest;
import com.wanted.codebombalms.inquiry.presentation.api.request.InquiryReplyRequest;
import com.wanted.codebombalms.inquiry.presentation.api.response.AdminInquiryDetailResponse;
import com.wanted.codebombalms.inquiry.presentation.api.response.AdminInquiryListResponse;
import com.wanted.codebombalms.inquiry.presentation.api.response.InquiryClassificationUpdateResponse;
import com.wanted.codebombalms.inquiry.presentation.api.response.InquiryFilterUpdateResponse;
import com.wanted.codebombalms.inquiry.presentation.api.response.InquiryReplyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - 문의", description = "관리자 문의 목록/상세 조회, 분류 수정, 필터링 처리, 답변 등록 API")
@RestController
@RequestMapping("/api/v1/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final GetAdminInquiriesUseCase getAdminInquiriesUseCase;
    private final GetAdminInquiryDetailUseCase getAdminInquiryDetailUseCase;
    private final UpdateInquiryClassificationUseCase updateInquiryClassificationUseCase;
    private final UpdateInquiryFilterUseCase updateInquiryFilterUseCase;
    private final ReplyInquiryUseCase replyInquiryUseCase;

    // 문의 목록 조회 (isFiltered로 정상/필터링 목록 구분, domain/severity/status 칼럼 필터)
    @Operation(summary = "관리자 문의 목록 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INQ-001: 페이지 요청 값이 올바르지 않습니다.")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<AdminInquiryListResponse>> findInquiries(
            @Parameter(description = "true면 AI가 필터링(스팸/무의미)한 목록, false면 정상 목록", example = "false")
            @RequestParam(defaultValue = "false") boolean isFiltered,
            @Parameter(description = InquiryDomain.SCHEMA_DESCRIPTION, example = "PROBLEMS")
            @RequestParam(required = false) InquiryDomain domain,
            @Parameter(description = InquirySeverity.SCHEMA_DESCRIPTION, example = "HIGH")
            @RequestParam(required = false) InquirySeverity severity,
            @Parameter(description = InquiryStatus.SCHEMA_DESCRIPTION, example = "OPEN")
            @RequestParam(required = false) InquiryStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var result = getAdminInquiriesUseCase.getInquiries(
                new GetAdminInquiriesQuery(isFiltered, domain, severity, status, page, size)
        );

        return ResponseEntity.ok(ApiResponse.success(
                InquiryResponseCode.RETRIEVED,
                InquiryResponseMessage.RETRIEVED,
                AdminInquiryListResponse.from(result)
        ));
    }

    // 문의 상세 조회 (원문, AI 분석, 관리자 처리 결과 전체)
    @Operation(summary = "관리자 문의 상세 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "INQ-002: 문의를 찾을 수 없습니다.")
    })
    @GetMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<AdminInquiryDetailResponse>> findInquiryDetail(
            @PathVariable Long inquiryId
    ) {
        var result = getAdminInquiryDetailUseCase.getInquiryDetail(inquiryId);

        return ResponseEntity.ok(ApiResponse.success(
                InquiryResponseCode.RETRIEVED,
                InquiryResponseMessage.RETRIEVED,
                AdminInquiryDetailResponse.from(result)
        ));
    }

    // 문의 분류(도메인/심각도) 수정, reason 필수
    @Operation(summary = "관리자 문의 분류 수정")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INQ-003: 문의 분류 수정 요청이 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "INQ-002: 문의를 찾을 수 없습니다.")
    })
    @PatchMapping("/{inquiryId}/classification")
    public ResponseEntity<ApiResponse<InquiryClassificationUpdateResponse>> updateInquiryClassification(
            @PathVariable Long inquiryId,
            @AuthenticationPrincipal Long adminId,
            @RequestBody InquiryClassificationUpdateRequest request
    ) {
        var result = updateInquiryClassificationUseCase.updateClassification(
                request.toCommand(inquiryId, adminId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                InquiryResponseCode.CLASSIFICATION_UPDATED,
                InquiryResponseMessage.CLASSIFICATION_UPDATED,
                InquiryClassificationUpdateResponse.from(result)
        ));
    }

    // 문의 필터링 처리 또는 복구, reason 필수
    @Operation(summary = "관리자 문의 필터링 처리/복구")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INQ-004: 문의 필터링 처리 요청이 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "INQ-002: 문의를 찾을 수 없습니다.")
    })
    @PatchMapping("/{inquiryId}/filter")
    public ResponseEntity<ApiResponse<InquiryFilterUpdateResponse>> updateInquiryFilter(
            @PathVariable Long inquiryId,
            @AuthenticationPrincipal Long adminId,
            @RequestBody InquiryFilterUpdateRequest request
    ) {
        var result = updateInquiryFilterUseCase.updateFilter(
                request.toCommand(inquiryId, adminId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                InquiryResponseCode.FILTER_UPDATED,
                InquiryResponseMessage.FILTER_UPDATED,
                InquiryFilterUpdateResponse.from(result)
        ));
    }

    // 답변 등록, 상태(ANSWERED)/노출 여부/답변자/답변시각까지 한 트랜잭션에서 갱신
    @Operation(summary = "관리자 문의 답변 등록")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INQ-005: 문의 답변 등록 요청이 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "INQ-002: 문의를 찾을 수 없습니다.")
    })
    @PostMapping("/{inquiryId}/reply")
    public ResponseEntity<ApiResponse<InquiryReplyResponse>> replyInquiry(
            @PathVariable Long inquiryId,
            @AuthenticationPrincipal Long adminId,
            @RequestBody InquiryReplyRequest request
    ) {
        var result = replyInquiryUseCase.reply(
                request.toCommand(inquiryId, adminId)
        );

        return ResponseEntity.status(201).body(ApiResponse.created(
                InquiryResponseCode.REPLIED,
                InquiryResponseMessage.REPLIED,
                InquiryReplyResponse.from(result)
        ));
    }
}
