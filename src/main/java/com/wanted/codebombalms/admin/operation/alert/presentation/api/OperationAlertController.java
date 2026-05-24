package com.wanted.codebombalms.admin.operation.alert.presentation.api;

import com.wanted.codebombalms.admin.operation.alert.application.query.GetOperationAlertsQuery;
import com.wanted.codebombalms.admin.operation.alert.application.usecase.DeleteOperationAlertUseCase;
import com.wanted.codebombalms.admin.operation.alert.application.usecase.GetOperationAlertDetailUseCase;
import com.wanted.codebombalms.admin.operation.alert.application.usecase.GetOperationAlertsUseCase;
import com.wanted.codebombalms.admin.operation.alert.application.usecase.UpdateOperationAlertMemoUseCase;
import com.wanted.codebombalms.admin.operation.alert.application.usecase.UpdateOperationAlertStatusUseCase;
import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;
import com.wanted.codebombalms.admin.operation.alert.presentation.api.request.OperationAlertMemoUpdateRequest;
import com.wanted.codebombalms.admin.operation.alert.presentation.api.request.OperationAlertStatusUpdateRequest;
import com.wanted.codebombalms.admin.operation.alert.presentation.api.response.OperationAlertDeleteResponse;
import com.wanted.codebombalms.admin.operation.alert.presentation.api.response.OperationAlertDetailResponse;
import com.wanted.codebombalms.admin.operation.alert.presentation.api.response.OperationAlertListResponse;
import com.wanted.codebombalms.admin.operation.alert.presentation.api.response.OperationAlertMemoUpdateResponse;
import com.wanted.codebombalms.admin.operation.alert.presentation.api.response.OperationAlertStatusUpdateResponse;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/admin/operation-alerts")
@RequiredArgsConstructor
public class OperationAlertController {

    private final GetOperationAlertsUseCase getOperationAlertsUseCase;
    private final GetOperationAlertDetailUseCase getOperationAlertDetailUseCase;
    private final UpdateOperationAlertStatusUseCase updateOperationAlertStatusUseCase;
    private final UpdateOperationAlertMemoUseCase updateOperationAlertMemoUseCase;
    private final DeleteOperationAlertUseCase deleteOperationAlertUseCase;


    //알람 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<OperationAlertListResponse>> findOperationAlerts(
            @RequestParam(required = false) OperationTargetType targetType,
            @RequestParam(required = false) OperationAlertStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var result = getOperationAlertsUseCase.getAlerts(
                new GetOperationAlertsQuery(targetType, status, page, size)
        );

        return ResponseEntity.ok(ApiResponse.success(
                OperationAlertResponseCode.RETRIEVED,
                OperationAlertResponseMessage.RETRIEVED,
                OperationAlertListResponse.from(result)
        ));
    }

    //알람 상세 조회
    @GetMapping("/{operationAlertId}")
    // 알림 ID로 운영 알림 상세 정보를 조회한다.
    public ResponseEntity<ApiResponse<OperationAlertDetailResponse>> findOperationAlertDetail(
            @PathVariable Long operationAlertId
    ) {
        var result = getOperationAlertDetailUseCase.getAlertDetail(operationAlertId);

        return ResponseEntity.ok(ApiResponse.success(
                OperationAlertResponseCode.RETRIEVED,
                OperationAlertResponseMessage.RETRIEVED,
                OperationAlertDetailResponse.from(result)
        ));
    }

    //알람 메모 수정
    @PatchMapping("/{operationAlertId}/memo")
    // 알림 ID로 운영 알림 관리자 메모를 수정한다.
    public ResponseEntity<ApiResponse<OperationAlertMemoUpdateResponse>> updateOperationAlertMemo(
            @PathVariable Long operationAlertId,
            @RequestBody OperationAlertMemoUpdateRequest request
    ) {
        var result = updateOperationAlertMemoUseCase.updateMemo(
                request.toCommand(operationAlertId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                OperationAlertResponseCode.UPDATED,
                OperationAlertResponseMessage.UPDATED,
                OperationAlertMemoUpdateResponse.from(result)
        ));
    }

    //알람 상태 변경
    @PatchMapping("/{operationAlertId}/status")
    public ResponseEntity<ApiResponse<OperationAlertStatusUpdateResponse>> updateOperationAlertStatus(
            @PathVariable Long operationAlertId,
            @AuthenticationPrincipal Long resolvedBy,
            @RequestBody OperationAlertStatusUpdateRequest request
    ) {
        var result = updateOperationAlertStatusUseCase.updateStatus(
                request.toCommand(operationAlertId, resolvedBy)
        );

        return ResponseEntity.ok(ApiResponse.success(
                OperationAlertResponseCode.UPDATED,
                OperationAlertResponseMessage.UPDATED,
                OperationAlertStatusUpdateResponse.from(result)
        ));
    }

    //알람 삭제
    @DeleteMapping("/{operationAlertId}")
    public ResponseEntity<ApiResponse<OperationAlertDeleteResponse>> deleteOperationAlert(
            @PathVariable Long operationAlertId
    ) {
        var result = deleteOperationAlertUseCase.delete(operationAlertId);

        return ResponseEntity.ok(ApiResponse.success(
                OperationAlertResponseCode.DELETED,
                OperationAlertResponseMessage.DELETED,
                OperationAlertDeleteResponse.from(result)
        ));
    }
}
