package com.wanted.codebombalms.domain.admin.operationalert.controller;

import com.wanted.codebombalms.domain.admin.automationrule.enums.OperationTargetType;
import com.wanted.codebombalms.domain.admin.operationalert.dto.request.OperationAlertStatusUpdateRequest;
import com.wanted.codebombalms.domain.admin.operationalert.enums.OperationAlertStatus;
import com.wanted.codebombalms.domain.admin.operationalert.service.OperationAlertService;
import com.wanted.codebombalms.global.common.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/operation-alerts")
@RequiredArgsConstructor
public class OperationAlertController {

    private final OperationAlertService operationAlertService;
    private static final Logger log = LoggerFactory.getLogger(OperationAlertController.class);

    @GetMapping
    public ResponseEntity<ResponseDTO> findOperationAlerts(
            @RequestParam(required = false) OperationTargetType targetType,
            @RequestParam(required = false) OperationAlertStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("[OperationAlertController] 운영 알림 목록 조회 요청 - targetType: {}, status: {}, page: {}, size: {}",
                targetType, status, page, size);

        return ResponseEntity.ok(new ResponseDTO(
                HttpStatus.OK,
                "운영 알림 목록 조회 성공",
                operationAlertService.findOperationAlerts(targetType, status, page, size)
        ));
    }

    @PatchMapping("/{operationAlertId}/status")
    public ResponseEntity<ResponseDTO> updateOperationAlertStatus(
            @PathVariable Long operationAlertId,
            @RequestBody OperationAlertStatusUpdateRequest request
    ) {
        log.info("[OperationAlertController] 운영 알림 상태 변경 요청 - operationAlertId: {}, status: {}, resolvedBy: {}",
                operationAlertId,
                request == null ? null : request.getStatus(),
                request == null ? null : request.getResolvedBy()
        );

        return ResponseEntity.ok(new ResponseDTO(
                HttpStatus.OK,
                "운영 알림 처리에 성공했습니다.",
                operationAlertService.updateOperationAlertStatus(operationAlertId, request)
        ));
    }
}