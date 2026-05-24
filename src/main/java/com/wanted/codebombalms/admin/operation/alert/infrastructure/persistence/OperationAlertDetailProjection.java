package com.wanted.codebombalms.admin.operation.alert.infrastructure.persistence;

import com.wanted.codebombalms.admin.operation.alert.domain.model.OperationAlertStatus;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationSeverity;
import com.wanted.codebombalms.admin.operation.common.domain.model.OperationTargetType;
import com.wanted.codebombalms.admin.operation.rule.domain.model.OperationRuleCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 알림과 자동 규칙을 조인한 상세 조회 JPQL 결과를 담는다.
public record OperationAlertDetailProjection(
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
        OperationRuleCode ruleCode,
        Integer minSampleCount
) {
}
