package com.wanted.codebombalms.admin.operation.alert.presentation.api;

import com.wanted.codebombalms.admin.operation.alert.application.query.GetOperationAlertsQuery;
import com.wanted.codebombalms.admin.operation.alert.application.usecase.GetOperationAlertsUseCase;
import com.wanted.codebombalms.admin.operation.alert.application.usecase.UpdateOperationAlertStatusUseCase;
import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;
import com.wanted.codebombalms.admin.operation.alert.presentation.api.request.OperationAlertStatusUpdateRequest;
import com.wanted.codebombalms.admin.operation.alert.presentation.api.response.OperationAlertListResponse;
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
    private final UpdateOperationAlertStatusUseCase updateOperationAlertStatusUseCase;


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
}
