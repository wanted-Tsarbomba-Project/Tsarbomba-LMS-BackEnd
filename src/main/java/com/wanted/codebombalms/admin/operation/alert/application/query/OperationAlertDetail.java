package com.wanted.codebombalms.admin.operation.alert.application.query;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationSeverity;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 운영 알림 상세 조회 화면에 필요한 알림, 규칙, 대상, 담당자, 지표 정보를 담는다.
public record OperationAlertDetail(
        Long operationAlertId,
        Long operationRuleId,
        OperationTargetType targetType,
        Long targetId,
        BigDecimal detectedValue,
        BigDecimal thresholdValueSnapshot,
        OperationSeverity severity,
        OperationAlertStatus status,
        Long assigneeId,
        String reason,
        String recommendedAction,
        LocalDateTime firstDetectedAt,
        LocalDateTime lastDetectedAt,
        Long resolvedBy,
        LocalDateTime resolvedAt,
        String adminMemo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        OperationAlertRuleInfo rule,
        OperationAlertTargetInfo target,
        OperationAlertAssigneeInfo assignee,
        OperationAlertMetricInfo metric
) {

    // 알림 기본 상세 정보에 대상 도메인 상세 정보를 결합한다.
    public OperationAlertDetail withTargetDetail(OperationAlertTargetDetail targetDetail) {
        return new OperationAlertDetail(
                operationAlertId,
                operationRuleId,
                targetType,
                targetId,
                detectedValue,
                thresholdValueSnapshot,
                severity,
                status,
                assigneeId,
                reason,
                recommendedAction,
                firstDetectedAt,
                lastDetectedAt,
                resolvedBy,
                resolvedAt,
                adminMemo,
                createdAt,
                updatedAt,
                rule,
                targetDetail.target(),
                targetDetail.assignee(),
                targetDetail.metric()
        );
    }
}
