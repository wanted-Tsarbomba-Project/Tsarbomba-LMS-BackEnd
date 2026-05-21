package com.wanted.codebombalms.admin.operation.alert.infrastructure.persistence;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationSeverity;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OperationAlertWithRuleProjection(
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
        LocalDateTime lastDetectedAt
) {
}