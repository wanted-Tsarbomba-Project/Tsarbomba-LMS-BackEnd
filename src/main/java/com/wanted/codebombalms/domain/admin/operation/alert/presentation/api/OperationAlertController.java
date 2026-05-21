package com.wanted.codebombalms.domain.admin.operation.alert.presentation.api;

import com.wanted.codebombalms.domain.admin.operation.alert.application.query.GetOperationAlertsQuery;
import com.wanted.codebombalms.domain.admin.operation.alert.application.usecase.GetOperationAlertsUseCase;
import com.wanted.codebombalms.domain.admin.operation.alert.application.usecase.UpdateOperationAlertStatusUseCase;
import com.wanted.codebombalms.domain.admin.operation.alert.domain.model.OperationAlertStatus;
import com.wanted.codebombalms.domain.admin.operation.alert.presentation.api.request.OperationAlertStatusUpdateRequest;
import com.wanted.codebombalms.domain.admin.operation.alert.presentation.api.response.OperationAlertListResponse;
import com.wanted.codebombalms.domain.admin.operation.alert.presentation.api.response.OperationAlertStatusUpdateResponse;
import com.wanted.codebombalms.domain.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.global.presentation.api.commonLegacy.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/operation-alerts")
@RequiredArgsConstructor
public class OperationAlertController {

    private final GetOperationAlertsUseCase getOperationAlertsUseCase;
    private final UpdateOperationAlertStatusUseCase updateOperationAlertStatusUseCase;

    @GetMapping
    public ResponseEntity<ResponseDTO> findOperationAlerts(
            @RequestParam(required = false) OperationTargetType targetType,
            @RequestParam(required = false) OperationAlertStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var result = getOperationAlertsUseCase.getAlerts(
                new GetOperationAlertsQuery(targetType, status, page, size)
        );

        return ResponseEntity.ok(new ResponseDTO(
                HttpStatus.OK,
                "운영 알림 목록 조회 성공",
                OperationAlertListResponse.from(result)
        ));
    }

    @PatchMapping("/{operationAlertId}/status")
    public ResponseEntity<ResponseDTO> updateOperationAlertStatus(
            @PathVariable Long operationAlertId,
            @RequestBody OperationAlertStatusUpdateRequest request
    ) {
        var result = updateOperationAlertStatusUseCase.updateStatus(
                request.toCommand(operationAlertId)
        );

        return ResponseEntity.ok(new ResponseDTO(
                HttpStatus.OK,
                "운영 알림 처리에 성공했습니다.",
                OperationAlertStatusUpdateResponse.from(result)
        ));
    }
}
